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

	public BindKey menuKey = new BindKey(BindKey.TYPE_KEY, GLFW.GLFW_KEY_RIGHT_ALT);
	public final List<Bind> binds = new ArrayList<>();

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
		} catch (Exception e) {
			BindMod.LOGGER.error("[Bind Mod] Не удалось прочитать конфиг {}", path, e);
		}
		return config;
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
