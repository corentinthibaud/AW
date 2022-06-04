package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.shadowmage.ancientwarfare.core.registry.ResearchRegistry;
import net.shadowmage.ancientwarfare.core.research.ResearchGoal;
import net.shadowmage.ancientwarfare.core.research.ResearchTracker;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ItemResearchNotes extends ItemBaseCore {

	private static final String RESEARCH_NAME_TAG = "researchName";
	private NonNullList<ItemStack> displayCache = null;

	public ItemResearchNotes() {
		super("research_note");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
		NBTTagCompound tag = stack.getTagCompound();
		String researchName = "corrupt_item";
		boolean known = false;
		if (tag != null && tag.hasKey(RESEARCH_NAME_TAG)) {
			String name = tag.getString(RESEARCH_NAME_TAG);
			if (ResearchRegistry.researchExists(name) && Minecraft.getMinecraft().player != null && world != null) {
				researchName = I18n.format(ResearchGoal.getUnlocalizedName(name));
				known = ResearchTracker.INSTANCE.hasPlayerCompleted(world, Minecraft.getMinecraft().player.getName(), name);
			} else {
				researchName = "missing_goal_for_id_" + researchName;
			}
		}
		tooltip.add(researchName);
		if (known) {
			tooltip.add(I18n.format("guistrings.research.known_research"));
			tooltip.add(I18n.format("guistrings.research.click_to_add_progress"));
		} else {
			tooltip.add(I18n.format("guistrings.research.unknown_research"));
			tooltip.add(I18n.format("guistrings.research.click_to_learn"));
		}
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (!isInCreativeTab(tab)) {
			return;
		}

		if (displayCache != null && !displayCache.isEmpty()) {
			items.addAll(displayCache);
			return;
		}
		displayCache = NonNullList.create();

		for (ResearchGoal goal : ResearchRegistry.getAllResearchGoals().stream().sorted(Comparator.comparing(ResearchGoal::getName))
				.collect(Collectors.toCollection(LinkedHashSet::new))) {
			ItemStack stack = new ItemStack(this);
			stack.setTagInfo(RESEARCH_NAME_TAG, new NBTTagString(goal.getName()));
			displayCache.add(stack);
			items.add(stack);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		NBTTagCompound tag = stack.getTagCompound();
		if (!world.isRemote && tag != null && tag.hasKey(RESEARCH_NAME_TAG)) {
			String name = tag.getString(RESEARCH_NAME_TAG);
			if (ResearchRegistry.researchExists(name)) {
				boolean known = ResearchTracker.INSTANCE.hasPlayerCompleted(player.world, player.getName(), name);
				if (!known) {
					if (ResearchTracker.INSTANCE.addResearchFromNotes(player.world, player.getName(), name)) {
						player.sendMessage(new TextComponentTranslation("guistrings.research.learned_from_item", net.minecraft.util.text.translation.I18n.translateToLocal(name)));
						stack.shrink(1);
					}
				} else {
					if (ResearchTracker.INSTANCE.addProgressFromNotes(player.world, player.getName(), name)) {
						player.sendMessage(new TextComponentTranslation("guistrings.research.added_progress", net.minecraft.util.text.translation.I18n.translateToLocal(name)));
						stack.shrink(1);
					}
				}
			}
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}
}
