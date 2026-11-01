package com.sokiki.bindmod.hud;

import com.sokiki.bindmod.BindModConfig;
import com.sokiki.bindmod.Waypoint;
import com.sokiki.bindmod.Waypoints;
import com.sokiki.bindmod.ui.Glass;
import com.sokiki.bindmod.ui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Компактный список меток с расстоянием и направлением.
 */
public final class WaypointHud {
	private WaypointHud() {
	}

	public static void render(GuiGraphics graphics, BindModConfig config) {
		Minecraft mc = Minecraft.getInstance();
		if (!config.waypointHud || mc.player == null || mc.options.hideGui) {
			return;
		}
		List<Waypoint> list = new ArrayList<>(Waypoints.visible(config));
		if (list.isEmpty()) {
			return;
		}
		list.sort(Comparator.comparingDouble(Waypoints::distanceTo));
		if (list.size() > 5) {
			list = list.subList(0, 5);
		}

		int rowH = 16;
		int width = 132;
		int height = list.size() * rowH + 24;
		int margin = 6;

		int right = config.hudCorner == 1 || config.hudCorner == 3
				? graphics.guiWidth() - width - margin
				: margin;
		int top;
		if (config.hudCorner == 2 || config.hudCorner == 3) {
			top = graphics.guiHeight() - height - margin;
		} else {
			top = margin;
			if (config.armorHud) {
				top += armorPanelHeight(mc) + 4;
			}
		}

		Glass.panel(graphics, right, top, width, height, 6);
		Glass.text(graphics, "МЕТКИ", right + 6, top + 6, Theme.MUTED);

		double yaw = Math.toRadians(mc.player.getYRot());
		for (int i = 0; i < list.size(); i++) {
			Waypoint waypoint = list.get(i);
			int rowY = top + 20 + i * rowH;

			// треугольник цвета метки
			int rgb = waypoint.rgb() | 0xFF000000;
			graphics.fill(right + 7, rowY + 4, right + 13, rowY + 9, rgb);

			String name = waypoint.name == null || waypoint.name.isBlank() ? "Метка" : waypoint.name;
			Glass.text(graphics, trim(name, 52), right + 17, rowY + 4, Theme.TEXT);
			String distance = Math.round(Waypoints.distanceTo(waypoint)) + "м " + arrow(mc, waypoint, yaw);
			Glass.textRight(graphics, distance, right + width - 6, rowY + 4, Theme.ACCENT);
		}
	}

	private static int armorPanelHeight(Minecraft mc) {
		int pieces = 0;
		for (var slot : new net.minecraft.world.entity.EquipmentSlot[]{
				net.minecraft.world.entity.EquipmentSlot.HEAD,
				net.minecraft.world.entity.EquipmentSlot.CHEST,
				net.minecraft.world.entity.EquipmentSlot.LEGS,
				net.minecraft.world.entity.EquipmentSlot.FEET}) {
			if (mc.player != null && !mc.player.getItemBySlot(slot).isEmpty()) {
				pieces++;
			}
		}
		return pieces == 0 ? 0 : pieces * 20 + 8 + 4;
	}

	private static String trim(String text, int width) {
		while (Glass.textWidth(text) > width && text.length() > 1) {
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

	/** Стрелка в сторону метки относительно взгляда игрока. */
	private static String arrow(Minecraft mc, Waypoint waypoint, double yaw) {
		if (mc.player == null) {
			return "";
		}
		double dx = waypoint.x - mc.player.getX();
		double dz = waypoint.z - mc.player.getZ();
		double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
		double relative = Mth.positiveModulo(angle - Math.toDegrees(yaw) + 180.0, 360.0);
		int sector = (int) Math.round(relative / 45.0) % 8;
		return switch (sector) {
			case 0 -> "▲";
			case 1 -> "◤";
			case 2 -> "◀";
			case 3 -> "◣";
			case 4 -> "▼";
			case 5 -> "◢";
			case 6 -> "▶";
			default -> "◥";
		};
	}
}
