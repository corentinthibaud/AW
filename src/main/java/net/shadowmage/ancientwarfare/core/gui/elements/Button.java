package net.shadowmage.ancientwarfare.core.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase.ActivationEvent;
import net.shadowmage.ancientwarfare.core.gui.Listener;
import net.shadowmage.ancientwarfare.core.util.RenderTools;

@SideOnly(Side.CLIENT)
public class Button extends GuiElement {

	private boolean pressed = false;
	protected String text;
	protected int textX;
	protected int textY;

	public Button(int topLeftX, int topLeftY, int width, int height, String text) {
		super(topLeftX, topLeftY, width, height);
		this.setText(text);
		this.setTooltipIfFound(text);
		this.addNewListener(new Listener(Listener.MOUSE_UP) {
			@Override
			public boolean onEvent(GuiElement widget, ActivationEvent evt) {
				if (pressed && enabled && visible && isMouseOverElement(evt.mx, evt.my)) {
					Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
					onPressed(evt.mButton);
				}
				pressed = false;
				return true;
			}
		});
		this.addNewListener(new Listener(Listener.MOUSE_DOWN) {
			@Override
			public boolean onEvent(GuiElement widget, ActivationEvent evt) {
				if (enabled && visible && isMouseOverElement(evt.mx, evt.my)) {
					pressed = true;
				}
				return true;
			}
		});
	}

	public final void setText(String text) {
		this.text = I18n.format(text);
		int tw = Minecraft.getMinecraft().fontRenderer.getStringWidth(this.text);
		if (tw > width) {
			width = tw;
		}
		textX = (width - tw) / 2;
		textY = (height - 8) / 2;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTick) {
		if (visible) {
			Minecraft.getMinecraft().renderEngine.bindTexture(widgetTexture1);
			int textureSize = 256;
			int startX = 0;
			int startY = enabled ? isMouseOverElement(mouseX, mouseY) ? 80 : 40 : 0;
			int textColor = startY == 80 ? 0xa0a0a0ff : 0xffffffff;//grey or white
			int usedWidth = 256;
			int usedHeight = 40;
			RenderTools.renderQuarteredTexture(textureSize, textureSize, startX, startY, usedWidth, usedHeight, renderX, renderY, width, height);
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, renderX + textX, renderY + textY, textColor);
			GlStateManager.color(1.f, 1.f, 1.f, 1.f);
		}
	}

	/*
	 * sub-classes may override this as an on-pressed callback
	 * method is called whenever the 'pressed' sound is played.
	 * uses built-in click listener for sound to trigger method
	 */
	protected void onPressed() {

	}

	/*
	 * Button-sensitive version of onPressed. 0 = LMB, 1 = RMB
	 * @param mButton
	 */
	protected void onPressed(int mButton) {
		onPressed(); // backwards compatibility
	}

}
