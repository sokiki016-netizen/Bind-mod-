package com.sokiki.bindmod.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Строка «подпись — значение — ползунок».
 */
public class SliderRow extends AbstractWidget {
	private final IntSupplier getter;
	private final IntConsumer setter;
	private final int min;
	private final int max;
	private final int step;
	private final String suffix;
	private boolean dragging;

	public SliderRow(int x, int y, int w, int h, String label, int min, int max, int step, String suffix,
					 IntSupplier getter, IntConsumer setter) {
		super(x, y, w, h, Component.literal(label));
		this.getter = getter;
		this.setter = setter;
		this.min = min;
		this.max = max;
		this.step = Math.max(1, step);
		this.suffix = suffix;
	}

	private int trackLeft() {
		return this.getX() + 10;
	}

	private int trackWidth() {
		return this.width - 20;
	}

	private void updateFromMouse(double mouseX) {
		double ratio = (mouseX - this.trackLeft()) / (double) this.trackWidth();
		ratio = Math.max(0.0, Math.min(1.0, ratio));
		int raw = this.min + (int) Math.round(ratio * (this.max - this.min));
		int snapped = this.min + Math.round((float) (raw - this.min) / this.step) * this.step;
		this.setter.accept(Math.max(this.min, Math.min(this.max, snapped)));
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		if (this.dragging) {
			if (Minecraft.getInstance().mouseHandler.isLeftPressed()) {
				this.updateFromMouse(mouseX);
			} else {
				this.dragging = false;
			}
		}

		boolean hover = this.isMouseOver(mouseX, mouseY) || this.dragging;
		Glass.round(graphics, this.getX(), this.getY(), this.width, this.height, 6,
				hover ? Theme.ROW_HOVER : Theme.ROW);

		int value = this.getter.getAsInt();
		Glass.text(graphics, this.getMessage().getString(), this.getX() + 10, this.getY() + 4, Theme.TEXT);
		Glass.textRight(graphics, value + this.suffix, this.getX() + this.width - 10, this.getY() + 4, Theme.ACCENT);

		int barY = this.getY() + this.height - 9;
		int bx = this.trackLeft();
		int bw = this.trackWidth();
		Glass.round(graphics, bx, barY, bw, 4, 2, 0x26FFFFFF);

		float ratio = (value - this.min) / (float) Math.max(1, this.max - this.min);
		int filled = Math.max(4, Math.round(bw * ratio));
		Glass.round(graphics, bx, barY, filled, 4, 2, Theme.ACCENT_SOFT + 0x20000000);

		int knobX = bx + filled - 3;
		Glass.round(graphics, knobX, barY - 3, 6, 10, 3, Theme.ACCENT);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.active || !this.visible || button != 0 || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		this.dragging = true;
		this.updateFromMouse(mouseX);
		return true;
	}
}
