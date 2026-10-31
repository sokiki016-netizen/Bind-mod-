package com.sokiki.bindmod.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public class GlassButton extends AbstractWidget {
	private final Runnable action;
	private final boolean accent;

	public GlassButton(int x, int y, int w, int h, String label, boolean accent, Runnable action) {
		super(x, y, w, h, Component.literal(label));
		this.accent = accent;
		this.action = action;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		boolean hover = this.isMouseOver(mouseX, mouseY);
		int fill;
		if (this.accent) {
			fill = hover ? 0x6664E1FF : 0x4464E1FF;
		} else {
			fill = hover ? Theme.ROW_HOVER : Theme.ROW;
		}
		Glass.round(graphics, this.getX(), this.getY(), this.width, this.height, 6, fill);
		int border = this.accent ? (hover ? 0x9964E1FF : 0x5564E1FF) : (hover ? Theme.BORDER : Theme.BORDER_SOFT);
		Glass.outline(graphics, this.getX(), this.getY(), this.width, this.height, 6, border);
		String label = this.getMessage().getString();
		int color = this.accent ? (hover ? Theme.ACCENT : 0xFFBFEFFF) : (hover ? Theme.TEXT : 0xFFC9D2E0);
		Glass.textCentered(graphics, label, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.active || !this.visible || button != 0 || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		this.action.run();
		return true;
	}
}
