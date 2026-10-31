package com.sokiki.bindmod.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public class TabButton extends AbstractWidget {
	private final Runnable action;
	private final boolean selected;

	public TabButton(int x, int y, int w, int h, String label, boolean selected, Runnable action) {
		super(x, y, w, h, Component.literal(label));
		this.selected = selected;
		this.action = action;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		boolean hover = this.isMouseOver(mouseX, mouseY);
		int fill = this.selected ? 0x2E64E1FF : (hover ? Theme.ROW_HOVER : 0x0DFFFFFF);
		Glass.round(graphics, this.getX(), this.getY(), this.width, this.height, 5, fill);
		if (this.selected) {
			Glass.round(graphics, this.getX(), this.getY() + 4, 2, this.height - 8, 1, Theme.ACCENT);
		}
		int color = this.selected ? Theme.TEXT : (hover ? 0xFFC9D2E0 : Theme.MUTED);
		Glass.text(graphics, this.getMessage().getString(), this.getX() + 12, this.getY() + (this.height - 8) / 2, color);
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
