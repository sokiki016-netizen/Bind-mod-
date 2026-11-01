package com.sokiki.bindmod.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Простые помощники для рисования «стеклянных» панелей со скруглёнными углами.
 */
public final class Glass {
	private Glass() {
	}

	/** Закруглённый прямоугольник (сплошная заливка). */
	public static void round(GuiGraphics graphics, int x, int y, int w, int h, int radius, int color) {
		if (w <= 0 || h <= 0 || (color >>> 24) == 0) {
			return;
		}
		int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
		if (r == 0) {
			graphics.fill(x, y, x + w, y + h, color);
			return;
		}
		for (int row = 0; row < h; row++) {
			double d;
			if (row < r) {
				d = r - row - 0.5;
			} else if (row >= h - r) {
				d = row - (h - r) + 0.5;
			} else {
				d = 0;
			}
			int inset = (int) Math.round(r - Math.sqrt(Math.max(0.0, r * (double) r - d * d)));
			graphics.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
		}
	}

	/** Контур закруглённого прямоугольника толщиной 1 px. */
	public static void outline(GuiGraphics graphics, int x, int y, int w, int h, int radius, int color) {
		if (w <= 0 || h <= 0 || (color >>> 24) == 0) {
			return;
		}
		int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
		if (r == 0) {
			graphics.fill(x, y, x + w, y + 1, color);
			graphics.fill(x, y + h - 1, x + w, y + h, color);
			graphics.fill(x, y, x + 1, y + h, color);
			graphics.fill(x + w - 1, y, x + w, y + h, color);
			return;
		}
		for (int row = 0; row < h; row++) {
			double d;
			if (row < r) {
				d = r - row - 0.5;
			} else if (row >= h - r) {
				d = row - (h - r) + 0.5;
			} else {
				d = 0;
			}
			int inset = (int) Math.round(r - Math.sqrt(Math.max(0.0, r * (double) r - d * d)));
			if (row == 0 || row == h - 1) {
				graphics.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
			} else {
				graphics.fill(x + inset, y + row, x + inset + 1, y + row + 1, color);
				graphics.fill(x + w - inset - 1, y + row, x + w - inset, y + row + 1, color);
			}
		}
	}

	/** Основная стеклянная панель: тень, стекло, контур, блик сверху. */
	public static void panel(GuiGraphics graphics, int x, int y, int w, int h, int radius) {
		round(graphics, x + 1, y + 3, w, h, radius, 0x40000000);
		round(graphics, x, y, w, h, radius, Theme.PANEL);
		round(graphics, x + 1, y + 1, w - 2, Math.max(0, h / 3), radius, Theme.PANEL_GLASS);
		outline(graphics, x, y, w, h, radius, Theme.BORDER);
		graphics.fill(x + radius, y + 1, x + w - radius, y + 2, Theme.HIGHLIGHT);
	}

	/** Внутренняя подложка (список, поле ввода). */
	public static void inner(GuiGraphics graphics, int x, int y, int w, int h, int radius) {
		round(graphics, x, y, w, h, radius, Theme.FIELD);
		outline(graphics, x, y, w, h, radius, Theme.BORDER_SOFT);
	}

	public static void text(GuiGraphics graphics, String text, int x, int y, int color) {
		graphics.drawString(Minecraft.getInstance().font, Component.literal(text), x, y, color, false);
	}

	public static void text(GuiGraphics graphics, String text, int x, int y, int color, boolean shadow) {
		graphics.drawString(Minecraft.getInstance().font, Component.literal(text), x, y, color, shadow);
	}

	public static int textWidth(String text) {
		return Minecraft.getInstance().font.width(text);
	}

	public static void textCentered(GuiGraphics graphics, String text, int centerX, int y, int color) {
		graphics.drawString(Minecraft.getInstance().font, Component.literal(text),
				centerX - textWidth(text) / 2, y, color, false);
	}

	public static void textRight(GuiGraphics graphics, String text, int rightX, int y, int color) {
		graphics.drawString(Minecraft.getInstance().font, Component.literal(text),
				rightX - textWidth(text), y, color, false);
	}

	/** Цвет прочности: зелёный → жёлтый → красный. */
	public static int durabilityColor(float ratio) {
		if (ratio > 0.6f) {
			return Theme.OK;
		}
		if (ratio > 0.25f) {
			return Theme.WARN;
		}
		return Theme.DANGER;
	}
}
