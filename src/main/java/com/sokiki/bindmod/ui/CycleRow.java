package com.sokiki.bindmod.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Строка с подписью и значением, которое переключается по клику.
 */
public class CycleRow extends AbstractWidget {
	private final IntSupplier getter;
	private final IntConsumer setter;
	private final String[] options;

	public CycleRow(int x, int y, int w, int h, String label, String[] options,
					IntSupplier getter, IntConsumer setter) {
		super(x, y, w, h, Component.literal(label));
		this.getter = getter;
		this.setter = setter;
		this.options = options;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		boolean hover = this.isMouseOver(mouseX, mouseY);
		Glass.round(graphics, this.getX(), this.getY(), this.width, this.height, 6,
				hover ? Theme.ROW_HOVER : Theme.ROW);
		Glass.text(graphics, this.getMessage().getString(), this.getX() + 10,
				this.getY() + (this.height - 8) / 2, Theme.TEXT);

		int index = Math.max(0, Math.min(this.options.length - 1, this.getter.getAsInt()));
		String value = this.options[index] + "  ›";
		int textW = Glass.textWidth(value);
		int bx = this.getX() + this.width - textW - 18;
		int by = this.getY() + 3;
		Glass.round(graphics, bx, by, textW + 12, this.height - 6, 5, 0x2E64E1FF);
		Glass.text(graphics, value, bx + 6, this.getY() + (this.height - 8) / 2,
				hover ? Theme.ACCENT : 0xFFBFEFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.active || !this.visible || button != 0 || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		int next = (this.getter.getAsInt() + 1) % this.options.length;
		this.setter.accept(next);
		return true;
	}
}
