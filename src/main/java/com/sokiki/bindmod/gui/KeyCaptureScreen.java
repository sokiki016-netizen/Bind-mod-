package com.sokiki.bindmod.gui;

import com.sokiki.bindmod.BindKey;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Экран «нажмите любую клавишу» — ловит клавишу или кнопку мыши.
 */
public class KeyCaptureScreen extends Screen {
	private final Screen parent;
	private final String prompt;
	private final Consumer<BindKey> onCapture;
	private boolean capturing = true;

	public KeyCaptureScreen(Screen parent, String prompt, Consumer<BindKey> onCapture) {
		super(Component.literal("Bind Mod"));
		this.parent = parent;
		this.prompt = prompt;
		this.onCapture = onCapture;
	}

	@Override
	protected void init() {
		this.clearWidgets();
		int cx = this.width / 2;
		this.addRenderableWidget(Button.builder(Component.literal("Отмена"), b -> this.minecraft.setScreen(this.parent))
				.bounds(cx - 60, this.height / 2 + 40, 120, 20).build());
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.minecraft.setScreen(this.parent);
			return true;
		}
		if (!this.capturing) {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		this.capturing = false;
		this.onCapture.accept(new BindKey(BindKey.TYPE_KEY, keyCode));
		return true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if (!this.capturing) {
			return false;
		}
		this.capturing = false;
		this.onCapture.accept(new BindKey(BindKey.TYPE_MOUSE, button));
		return true;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, 0xE0101010);
		super.render(graphics, mouseX, mouseY, partialTick);
		int cx = this.width / 2;
		int cy = this.height / 2;

		graphics.drawCenteredString(this.font, Component.literal("§l§b" + this.prompt), cx, cy - 30, 0xFFFFFF);
		graphics.drawCenteredString(this.font, Component.literal("§7Нажмите любую клавишу или кнопку мыши"), cx, cy - 6, 0xA0A0A0);
		graphics.drawCenteredString(this.font, Component.literal("§8Esc — отмена"), cx, cy + 12, 0x707070);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
