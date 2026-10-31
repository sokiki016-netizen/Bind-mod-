package com.sokiki.bindmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Конфиг мода: config/bindmod.json
 */
public final class BindModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final int MODE_SNEAK = 0;
	public static final int MODE_JUMP = 1;

	public BindKey menuKey = new BindKey(BindKey.TYPE_KEY, GLFW.GLFW_KEY_RIGHT_ALT);
	public BindKey waypointKey = new BindKey(BindKey.TYPE_KEY, GLFW.GLFW_KEY_G);
	public final List<Bind> binds = new ArrayList<>();
	public final List<Waypoint> waypoints = new ArrayList<>();

	// HUD
	public boolean armorHud = true;
	public boolean waypointHud = true;
	public boolean waypointsEnabled = true;
	public int hudCorner = 0; // 0 — слева сверху, 1 — справа сверху, 2 — слева снизу, 3 — справа снизу

	// Анти-АФК
	public boolean antiAfk = false;
	public int antiAfkInterval = 60; // секунды
	public int antiAfkMode = MODE_SNEAK;

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("bindmod.json");
	}

	public static BindModConfig load() {
		BindModConfig config = new BindModConfig();
		Path path = path();
		if (!Files.exists(path)) {
			return config;
		}
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonElement root = JsonParser.parseReader(reader);
			if (!root.isJsonObject()) {
				return config;
			}
			JsonObject object = root.getAsJsonObject();
			if (object.has("menuKey") && object.get("menuKey").isJsonPrimitive()) {
				BindKey key = BindKey.deserialize(object.get("menuKey").getAsString());
				if (key != null && key.isSet()) {
					config.menuKey = key;
				}
			}
			if (object.has("waypointKey") && object.get("waypointKey").isJsonPrimitive()) {
				BindKey key = BindKey.deserialize(object.get("waypointKey").getAsString());
				if (key != null && key.isSet()) {
					config.waypointKey = key;
				}
			}
			if (object.has("waypoints") && object.get("waypoints").isJsonArray()) {
				for (JsonElement element : object.getAsJsonArray("waypoints")) {
					if (!element.isJsonObject()) {
						continue;
					}
					JsonObject waypointObject = element.getAsJsonObject();
					try {
						String name = waypointObject.has("name") ? waypointObject.get("name").getAsString() : "Метка";
						double wx = waypointObject.get("x").getAsDouble();
						double wy = waypointObject.get("y").getAsDouble();
						double wz = waypointObject.get("z").getAsDouble();
						int color = readInt(waypointObject, "color", config.waypoints.size());
						String dimension = waypointObject.has("dimension")
								? waypointObject.get("dimension").getAsString() : "";
						config.waypoints.add(new Waypoint(name, wx, wy, wz, color, dimension));
					} catch (Exception ignored) {
						BindMod.LOGGER.warn("[Bind Mod] Пропущена битая метка в конфиге");
					}
				}
			}
			if (object.has("binds") && object.get("binds").isJsonArray()) {
				JsonArray array = object.getAsJsonArray("binds");
				for (JsonElement element : array) {
					if (!element.isJsonObject()) {
						continue;
					}
					JsonObject bindObject = element.getAsJsonObject();
					String keyRaw = bindObject.has("key") ? bindObject.get("key").getAsString() : null;
					String command = bindObject.has("command") ? bindObject.get("command").getAsString() : "";
					BindKey key = BindKey.deserialize(keyRaw);
					if (key == null) {
						continue;
					}
					config.binds.add(new Bind(key, command));
				}
			}
			if (object.has("hud") && object.get("hud").isJsonObject()) {
				JsonObject hud = object.getAsJsonObject("hud");
				config.armorHud = readBoolean(hud, "armorDurability", config.armorHud);
				config.waypointHud = readBoolean(hud, "waypointList", config.waypointHud);
				config.waypointsEnabled = readBoolean(hud, "waypointsInWorld", config.waypointsEnabled);
				config.hudCorner = clamp(readInt(hud, "corner", config.hudCorner), 0, 3);
			}
			if (object.has("antiAfk") && object.get("antiAfk").isJsonObject()) {
				JsonObject antiAfk = object.getAsJsonObject("antiAfk");
				config.antiAfk = readBoolean(antiAfk, "enabled", config.antiAfk);
				config.antiAfkInterval = clamp(readInt(antiAfk, "intervalSeconds", config.antiAfkInterval), 5, 600);
				config.antiAfkMode = clamp(readInt(antiAfk, "mode", config.antiAfkMode), 0, 1);
			}
		} catch (Exception e) {
			BindMod.LOGGER.error("[Bind Mod] Не удалось прочитать конфиг {}", path, e);
		}
		return config;
	}

	private static boolean readBoolean(JsonObject object, String name, boolean fallback) {
		try {
			return object.has(name) ? object.get(name).getAsBoolean() : fallback;
		} catch (Exception e) {
			return fallback;
		}
	}

	private static int readInt(JsonObject object, String name, int fallback) {
		try {
			return object.has(name) ? object.get(name).getAsInt() : fallback;
		} catch (Exception e) {
			return fallback;
		}
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	public void save() {
		JsonObject object = new JsonObject();
		object.addProperty("menuKey", this.menuKey.serialize());

		JsonArray array = new JsonArray();
		for (Bind bind : this.binds) {
			JsonObject bindObject = new JsonObject();
			bindObject.addProperty("key", bind.key.serialize());
			bindObject.addProperty("command", bind.command);
			array.add(bindObject);
		}
		object.add("binds", array);
		object.addProperty("waypointKey", this.waypointKey.serialize());

		JsonArray waypointArray = new JsonArray();
		for (Waypoint waypoint : this.waypoints) {
			JsonObject waypointObject = new JsonObject();
			waypointObject.addProperty("name", waypoint.name);
			waypointObject.addProperty("x", waypoint.x);
			waypointObject.addProperty("y", waypoint.y);
			waypointObject.addProperty("z", waypoint.z);
			waypointObject.addProperty("color", waypoint.color);
			waypointObject.addProperty("dimension", waypoint.dimension == null ? "" : waypoint.dimension);
			waypointArray.add(waypointObject);
		}
		object.add("waypoints", waypointArray);

		JsonObject hud = new JsonObject();
		hud.addProperty("armorDurability", this.armorHud);
		hud.addProperty("waypointList", this.waypointHud);
		hud.addProperty("waypointsInWorld", this.waypointsEnabled);
		hud.addProperty("corner", this.hudCorner);
		object.add("hud", hud);

		JsonObject antiAfk = new JsonObject();
		antiAfk.addProperty("enabled", this.antiAfk);
		antiAfk.addProperty("intervalSeconds", this.antiAfkInterval);
		antiAfk.addProperty("mode", this.antiAfkMode);
		object.add("antiAfk", antiAfk);

		try {
			Path path = path();
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(object, writer);
			}
		} catch (Exception e) {
			BindMod.LOGGER.error("[Bind Mod] Не удалось сохранить конфиг", e);
		}
	}
}
