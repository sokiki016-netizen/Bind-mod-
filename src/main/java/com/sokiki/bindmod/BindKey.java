package com.sokiki.bindmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Клавиша или кнопка мыши, на которую можно повесить бинд.
 */
public final class BindKey {
	public static final int TYPE_KEY = 0;
	public static final int TYPE_MOUSE = 1;

	public int type;
	public int code;

	public BindKey(int type, int code) {
		this.type = type;
		this.code = code;
	}

	public boolean isSet() {
		return this.code >= 0;
	}

	public boolean isMouse() {
		return this.type == TYPE_MOUSE;
	}

	public boolean isDown() {
		Minecraft mc = Minecraft.getInstance();
		if (mc == null || mc.getWindow() == null) {
			return false;
		}
		long handle = mc.getWindow().getWindow();
		if (handle == 0L || !this.isSet()) {
			return false;
		}
		if (this.isMouse()) {
			return GLFW.glfwGetMouseButton(handle, this.code) == GLFW.GLFW_PRESS;
		}
		return GLFW.glfwGetKey(handle, this.code) == GLFW.GLFW_PRESS;
	}

	public boolean sameAs(BindKey other) {
		return other != null && other.type == this.type && other.code == this.code;
	}

	public String displayName() {
		if (!this.isSet()) {
			return "—";
		}
		InputConstants.Key key = this.isMouse()
				? InputConstants.Type.MOUSE.getOrCreate(this.code)
				: InputConstants.Type.KEYSYM.getOrCreate(this.code);
		return key.getDisplayName().getString();
	}

	public String serialize() {
		return (this.isMouse() ? "mouse" : "key") + ":" + this.code;
	}

	public static BindKey deserialize(String raw) {
		if (raw == null || raw.isEmpty()) {
			return null;
		}
		try {
			int colon = raw.indexOf(':');
			String kind = colon < 0 ? "key" : raw.substring(0, colon);
			String value = colon < 0 ? raw : raw.substring(colon + 1);
			int type = "mouse".equalsIgnoreCase(kind) ? TYPE_MOUSE : TYPE_KEY;
			return new BindKey(type, Integer.parseInt(value.trim()));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public BindKey copy() {
		return new BindKey(this.type, this.code);
	}
}
