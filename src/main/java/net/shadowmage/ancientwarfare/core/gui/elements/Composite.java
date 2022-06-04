package net.shadowmage.ancientwarfare.core.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase.ActivationEvent;
import net.shadowmage.ancientwarfare.core.gui.Listener;
import net.shadowmage.ancientwarfare.core.util.RenderTools;

import java.util.ArrayList;
import java.util.List;

/*
 * base class for gui elements that contain other elements
 * E.G. Scrollable area, Tabbed area, Multi-button controls
 *
 * @author John
 */
@SideOnly(Side.CLIENT)
public class Composite extends GuiElement {

	protected List<GuiElement> elements = new ArrayList<>();

	private GuiContainerBase gui;

	public GuiContainerBase getGui() {
		return gui;
	}

	public Composite(GuiContainerBase gui, int topLeftX, int topLeftY, int width, int height) {
		super(topLeftX, topLeftY, width, height);
		this.gui = gui;
		addDefaultListeners();
		this.keyboardInterface = true;
		this.mouseInterface = true;
	}

	/*
	 * sub-classes should override this method to add their custom default listeners. *
	 */
	protected void addDefaultListeners() {
		this.addNewListener(new Listener(Listener.ALL_EVENTS) {
			@Override
			public boolean onEvent(GuiElement widget, ActivationEvent evt) {
				if ((evt.type & Listener.KEY_TYPES) != 0) {
					for (GuiElement element : elements) {
						element.handleKeyboardInput(evt);
					}
				} else if ((evt.type & Listener.MOUSE_TYPES) != 0) {
					if (isMouseOverElement(evt.mx, evt.my)) {
						/*
						 * adjust mouse event position for relative to composite
                         */
						int x = evt.mx;
						int y = evt.my;
						for (GuiElement element : elements) {
							element.handleMouseInput(evt);
						}
						evt.mx = x;
						evt.my = y;
					} else if (evt.type == Listener.MOUSE_UP) {
						for (GuiElement element : elements) {
							element.setSelected(false);
						}
					}
				}
				return true;
			}
		});
	}

	public void clearElements() {
		this.gui.onCompositeCleared(elements);
		this.elements.clear();
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTick) {
		if (!isMouseOverElement(mouseX, mouseY)) {
			mouseX = Integer.MIN_VALUE;
			mouseY = Integer.MIN_VALUE;
		}
		setViewport();
		if (drawBackground()) {
			Minecraft.getMinecraft().renderEngine.bindTexture(backgroundTextureLocation);
			RenderTools.renderQuarteredTexture(256, 256, 0, 0, 256, 240, renderX, renderY, width, height);
		}
		for (GuiElement element : this.elements) {
			element.render(mouseX, mouseY, partialTick);
		}
		popViewport();
	}

	protected boolean drawBackground() {
		return true;
	}

	public void addGuiElement(GuiElement element) {
		if (!this.elements.contains(element)) {
			this.elements.add(element);
		}
	}

	protected void setViewport() {
		int x = renderX + getPaddingX();
		int y = renderY + getPaddingY();
		int w = width - 2 * getPaddingX();
		int h = height - 2 * getPaddingY();
		GuiContainerBase.pushViewport(x, y, w, h);
	}

	protected int getPaddingY() {
		return 3;
	}

	protected int getPaddingX() {
		return 3;
	}

	protected void popViewport() {
		GuiContainerBase.popViewport();
	}

	/*
	 * sub-classes of Composite should override this method
	 * to feed their current scrolled position into their elements
	 * (e.g. scrollbar composite will use scrollbar pos)
	 */
	@Override
	public void updateGuiPosition(int guiLeft, int guiTop) {
		super.updateGuiPosition(guiLeft, guiTop);
		updateElementPositions();
	}

	protected void updateElementPositions() {
		for (GuiElement element : this.elements) {
			element.updateGuiPosition(renderX, renderY);
		}
	}

	public boolean isMouseOverSubElement(int mx, int my) {
		for (GuiElement el : this.elements) {
			if (el.isMouseOverElement(mx, my)) {
				return true;
			}
		}
		return false;
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

}
