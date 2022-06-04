package net.shadowmage.ancientwarfare.structure.template.load;

import net.minecraft.util.math.Vec3i;
import net.shadowmage.ancientwarfare.core.util.CompatUtils;
import net.shadowmage.ancientwarfare.core.util.StringTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.api.TemplateParsingException;
import net.shadowmage.ancientwarfare.structure.api.TemplateParsingException.TemplateRuleParsingException;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleBlock;
import net.shadowmage.ancientwarfare.structure.api.TemplateRuleEntityBase;
import net.shadowmage.ancientwarfare.structure.template.StructurePluginManager;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate.Version;
import net.shadowmage.ancientwarfare.structure.template.build.validation.StructureValidator;
import net.shadowmage.ancientwarfare.structure.template.datafixes.FixResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TemplateParser {

	public static final TemplateParser INSTANCE = new TemplateParser();

	private TemplateParser() {
	}

	Optional<FixResult<StructureTemplate>> parseTemplate(String fileName, List<String> templateLines) {
		try {
			return parseTemplateLines(fileName, templateLines);
		}
		catch (TemplateParsingException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	private Optional<FixResult<StructureTemplate>> parseTemplateLines(String fileName, List<String> lines) throws TemplateParsingException {
		Iterator<String> it = lines.iterator();
		String line;

		StructureValidator validation = null;
		List<String> groupedLines = new ArrayList<>();

		String name = "";
		Version version = Version.NONE;
		Vec3i size = new Vec3i(0, 0, 0);
		Vec3i offset = new Vec3i(0, 0, 0);
		short[] templateData = null;
		boolean[] initData = new boolean[4];
		Map<Integer, TemplateRuleBlock> parsedRules = new HashMap<>();
		Map<Integer, TemplateRuleEntityBase> parsedEntities = new HashMap<>();
		FixResult.Builder<StructureTemplate> resultBuilder = new FixResult.Builder<>();
		String[] modDependencies = new String[0];
		while (it.hasNext()) {
			line = it.next();
			if (line.startsWith("#") || line.equals("")) {
				continue;
			}
			if (line.startsWith("header:")) {
				while (it.hasNext()) {
					line = it.next();
					if (line.startsWith(":endheader")) {
						break;
					}
					if (line.startsWith("version=")) {
						initData[0] = true;
						version = new Version(StringTools.safeParseString("=", line));
					}
					if (line.startsWith("name=")) {
						name = StringTools.safeParseString("=", line);
						initData[1] = true;
					}
					if (line.startsWith("mods=")) {
						modDependencies = StringTools.safeParseString("=", line).split(",");
						if (!CompatUtils.areModsLoaded(modDependencies)) {
							AncientWarfareStructure.LOG.info("Template {} not loaded because it depends on mod that isn't loaded.", fileName);
							return Optional.empty();
						}
					}
					if (line.startsWith("size=")) {
						int[] sizes = StringTools.safeParseIntArray("=", line);
						size = new Vec3i(sizes[0], sizes[1], sizes[2]);
						initData[2] = true;
					}
					if (line.startsWith("offset=")) {
						int[] offsets = StringTools.safeParseIntArray("=", line);
						offset = new Vec3i(offsets[0], offsets[1], offsets[2]);
						initData[3] = true;
					}
				}
				for (int i = 0; i < 4; i++) {
					if (!initData[i]) {
						throw new TemplateParsingException("Could not parse template for " + fileName + " -- template was missing header or header data.");
					}
				}
				templateData = new short[size.getX() * size.getY() * size.getZ()];
			}

			/*
			 * parse out validation data
             */
			if (line.startsWith("validation:")) {
				while (it.hasNext()) {
					line = it.next();
					if (line.startsWith(":endvalidation")) {
						break;
					}
					groupedLines.add(line);
				}
				validation = StructureValidator.parseValidator(groupedLines);
				groupedLines.clear();
			}

            /*
			 * parse out rule data
             */
			if (line.startsWith("rule:")) {
				groupedLines.add(line);
				while (it.hasNext()) {
					line = it.next();
					groupedLines.add(line);
					if (line.startsWith(":endrule")) {
						break;
					}
				}
				try {
					TemplateRuleBlock parsedRule = resultBuilder.updateAndGetData(StructurePluginManager.getRule(version, groupedLines, "rule"));
					parsedRules.put(parsedRule.ruleNumber, parsedRule);
				}
				catch (TemplateRuleParsingException e) {
					StringBuilder data = new StringBuilder(e.getMessage() + "\n");
					for (String line1 : groupedLines) {
						data.append(line1).append("\n");
					}
					TemplateRuleParsingException e1 = new TemplateRuleParsingException(data.toString(), e);
					AncientWarfareStructure.LOG.error("Caught exception parsing template rule for structure: " + name, e1.getMessage());
				}
				groupedLines.clear();
			}

            /*
			 * parse out rule data
             */
			if (line.startsWith("entity:")) {
				groupedLines.add(line);
				while (it.hasNext()) {
					line = it.next();
					groupedLines.add(line);
					if (line.startsWith(":endentity")) {
						break;
					}
				}
				try {
					TemplateRuleEntityBase entityRule = resultBuilder.updateAndGetData(StructurePluginManager.getRule(version, groupedLines, "entity"));
					parsedEntities.put(entityRule.ruleNumber, entityRule);
				}
				catch (TemplateRuleParsingException e) {
					StringBuilder data = new StringBuilder(e.getMessage() + "\n");
					for (String line1 : groupedLines) {
						data.append(line1).append("\n");
					}
					TemplateRuleParsingException e1 = new TemplateRuleParsingException(data.toString(), e);
					AncientWarfareStructure.LOG.error("Caught exception parsing template rule for structure: " + name, e1.getMessage());
				}
				groupedLines.clear();
			}

            /*
			 * parse out layer data
             */
			if (line.startsWith("layer:")) {
				groupedLines.add(line);
				while (it.hasNext()) {
					line = it.next();
					groupedLines.add(line);
					if (line.startsWith(":endlayer")) {
						break;
					}
				}
				parseLayer(groupedLines, size, templateData);
				groupedLines.clear();
			}
		}

		return Optional.of(resultBuilder.build(constructTemplate(name, modDependencies, version, size, offset, templateData, parsedRules, parsedEntities, validation)));
	}

	private StructureTemplate constructTemplate(String name, String[] modDependencies, Version version, Vec3i size, Vec3i offset, short[] templateData, Map<Integer, TemplateRuleBlock> rules, Map<Integer, TemplateRuleEntityBase> entityRules, StructureValidator validation) {
		StructureTemplate template = new StructureTemplate(name, Arrays.stream(modDependencies).collect(Collectors.toSet()), version, size, offset);
		template.setBlockRules(rules);
		template.setEntityRules(entityRules);
		template.setTemplateData(templateData);
		template.setValidationSettings(validation);
		return template;
	}

	/*
	 * should parse layer and insert direcly into templateData
	 */
	private void parseLayer(List<String> templateLines, Vec3i size, short[] templateData) {
		int minLayer = 0;
		int maxLayer = 0;
		List<String> rowLines = new ArrayList<>();
		for (String st : templateLines) {
			if (!st.startsWith(":endlayer")) {
				if (st.startsWith("layer:")) {
					String[] layerIds = st.split(":")[1].split("-");
					minLayer = Integer.parseInt(layerIds[0].trim());
					maxLayer = layerIds.length > 1 ? Integer.parseInt(layerIds[1].trim()) : minLayer;
				} else {
					rowLines.add(st);
				}
			}
		}
		parseLayer(size, templateData, minLayer, maxLayer, rowLines);
	}

	private void parseLayer(Vec3i size, short[] templateData, int minLayer, int maxLayer, List<String> rowLines) {
		List<short[]> rows = parseLayerRows(rowLines);
		for (int layerId = minLayer; layerId <= maxLayer; layerId++) {
			int z = 0;
			for (short[] data : rows) {
				for (int x = 0; x < size.getX() && x < data.length; x++) {
					templateData[StructureTemplate.getIndex(new Vec3i(x, layerId, z), size)] = data[x];
				}
				z++;
			}
		}
	}

	private List<short[]> parseLayerRows(List<String> rowLines) {
		List<short[]> rows = new ArrayList<>();
		for (String rowLine : rowLines) {
			String[] rowParts = rowLine.split("x");
			int repeat = 1;
			short[] blocks;
			if (rowParts.length > 1) {
				repeat = Integer.parseInt(rowParts[0]);
				blocks = parseBlocks(rowParts[1]);
			} else {
				blocks = parseBlocks(rowParts[0]);
			}
			for (int i = 0; i < repeat; i++) {
				rows.add(blocks);
			}
		}

		return rows;
	}

	private short[] parseBlocks(String row) {
		List<Short> blocks = new ArrayList<>();

		String[] blockParts = row.split(",");

		for (String blockPart : blockParts) {
			String[] blockDef = blockPart.split("\\|");
			int repeat = 1;
			if (blockDef.length > 1) {
				repeat = Integer.parseInt(blockDef[1]);
			}
			short id = Short.parseShort(blockDef[0]);
			for (int i = 0; i < repeat; i++) {
				blocks.add(id);
			}
		}

		short[] ret = new short[blocks.size()];
		int i = 0;
		for (short block : blocks) {
			ret[i++] = block;
		}
		return ret;
	}
}
