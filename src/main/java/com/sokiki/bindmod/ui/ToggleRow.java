package com.sokiki.bindmod.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Строка с подписью и тумблером справа (в стиле современных клиентов).
 */
public class ToggleRow extends AbstractWidget {
	private final BooleanSupplier getter;
	private final Consumer<Boolean> setter;
	private float anim = -1f;

	public ToggleRow(int x, int y, int w, int h, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
		super(x, y, w, h, Component.literal(label));
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		boolean on = this.getter.getAsBoolean();
		float target = on ? 1f : 0f;
		if (this.anim < 0f) {
			this.anim = target;
		}
		this.anim += (target - this.anim) * 0.35f;

		boolean hover = this.isMouseOver(mouseX, mouseY);
		Glass.round(graphics, this.getX(), this.getY(), this.width, this.height, 6,
				hover ? Theme.ROW_HOVER : Theme.ROW);
		Glass.text(graphics, this.getMessage().getString(), this.getX() + 10,
				this.getY() + (this.height - 8) / 2, on ? Theme.TEXT : Theme.MUTED);

		int trackW = 24;
		int trackH = 12;
		int tx = this.getX() + this.width - trackW - 10;
		int ty = this.getY() + (this.height - trackH) / 2;
		Glass.round(graphics, tx, ty, trackW, trackH, trackH / 2, on ? 0x8064E1FF : 0x26FFFFFF);
		Glass.outline(graphics, tx, ty, trackW, trackH, trackH / 2, on ? 0x9964E1FF : Theme.BORDER_SOFT);

		int knob = 8;
		int kx = tx + 2 + Math.round((trackW - knob - 4) * this.anim);
		Glass.round(graphics, kx, ty + 2, knob, knob, knob / 2, on ? Theme.ACCENT : 0xFFA9B3C4);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.active || !this.visible || button != 0 || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		this.setter.accept(!this.getter.getAsBoolean());
		return true;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}
}
