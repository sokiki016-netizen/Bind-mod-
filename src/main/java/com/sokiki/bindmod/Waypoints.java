package com.sokiki.bindmod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Хелперы для меток: поставить у себя, следующее имя/цвет.
 */
public final class Waypoints {
	private Waypoints() {
	}

	public static String currentDimension() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return "";
		}
		return mc.level.dimension().location().toString();
	}

	public static String nextName(BindModConfig config) {
		int index = config.waypoints.size() + 1;
		return "Метка " + index;
	}

	public static Waypoint addHere(BindModConfig config) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return null;
		}
		Vec3 pos = mc.player.position();
		Waypoint waypoint = new Waypoint(nextName(config), pos.x, pos.y, pos.z,
				config.waypoints.size(), currentDimension());
		config.waypoints.add(waypoint);
		config.save();
		return waypoint;
	}

	public static void notifyPlayer(String message) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			mc.player.displayClientMessage(Component.literal(message), false);
		}
	}

	/** Метки текуйщего измерения, ближайшие первыми. */
	public static List<Waypoint> visible(BindModConfig config) {
		String dimension = currentDimension();
		return config.waypoints.stream()
				.filter(waypoint -> waypoint.dimension == null || waypoint.dimension.isEmpty()
						|| waypoint.dimension.equals(dimension))
				.toList();
	}

	public static double distanceTo(Waypoint waypoint) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return 0;
		}
		return Math.sqrt(mc.player.distanceToSqr(waypoint.x, waypoint.y, waypoint.z));
	}
}
