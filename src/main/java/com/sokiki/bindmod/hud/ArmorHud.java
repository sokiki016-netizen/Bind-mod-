package com.sokiki.bindmod.hud;

import com.sokiki.bindmod.BindModConfig;
import com.sokiki.bindmod.ui.Glass;
import com.sokiki.bindmod.ui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Небольшая панель с прочностью надетой брони.
 */
public final class ArmorHud {
	private static final EquipmentSlot[] SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private ArmorHud() {
	}

	public static void render(GuiGraphics graphics, BindModConfig config) {
		Minecraft mc = Minecraft.getInstance();
		if (!config.armorHud || mc.player == null || mc.options.hideGui) {
			return;
		}

		List<ItemStack> pieces = new ArrayList<>(4);
		for (EquipmentSlot slot : SLOTS) {
			ItemStack stack = mc.player.getItemBySlot(slot);
			if (!stack.isEmpty()) {
				pieces.add(stack);
			}
		}
		if (pieces.isEmpty()) {
			return;
		}

		int rowH = 20;
		int panelW = 82;
		int pad = 4;
		int panelH = pieces.size() * rowH + pad * 2;

		int margin = 6;
		int right = config.hudCorner == 1 || config.hudCorner == 3
				? graphics.guiWidth() - panelW - margin
				: margin;
		int top = config.hudCorner == 2 || config.hudCorner == 3
				? graphics.guiHeight() - panelH - margin
				: margin;

		Glass.panel(graphics, right, top, panelW, panelH, 6);

		for (int i = 0; i < pieces.size(); i++) {
			ItemStack stack = pieces.get(i);
			int rowY = top + pad + i * rowH;
			int iconX = right + pad + 1;
			int iconY = rowY + 2;

			graphics.renderItem(stack, iconX, iconY);
			graphics.renderItemDecorations(mc.font, stack, iconX, iconY);

			String label;
			int color;
			if (stack.isDamageableItem()) {
				float ratio = 1.0f - stack.getDamageValue() / (float) Math.max(1, stack.getMaxDamage());
				int percent = Math.round(ratio * 100.0f);
				label = percent + "%";
				color = Glass.durabilityColor(ratio);
			} else {
				label = "∞";
				color = Theme.MUTED;
			}
			Glass.textRight(graphics, label, right + panelW - pad - 2, rowY + 6, color);
		}
	}
}
