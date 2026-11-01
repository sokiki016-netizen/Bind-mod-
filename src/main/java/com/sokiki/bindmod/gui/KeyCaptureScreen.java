package com.sokiki.bindmod.gui;

import com.sokiki.bindmod.BindKey;
import com.sokiki.bindmod.ui.Glass;
import com.sokiki.bindmod.ui.GlassButton;
import com.sokiki.bindmod.ui.Theme;
import net.minecraft.client.gui.GuiGraphics;
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
	private int x0;
	private int y0;
	private int panelW = 300;
	private int panelH = 118;

	public KeyCaptureScreen(Screen parent, String prompt, Consumer<BindKey> onCapture) {
		super(Component.literal("Bind Mod"));
		this.parent = parent;
		this.prompt = prompt;
		this.onCapture = onCapture;
	}

	@Override
	protected void init() {
		this.clearWidgets();
		this.panelW = Math.min(300, this.width - 40);
		this.panelH = 118;
		this.x0 = (this.width - this.panelW) / 2;
		this.y0 = (this.height - this.panelH) / 2;

		this.addRenderableWidget(new GlassButton(this.x0 + this.panelW / 2 - 50, this.y0 + this.panelH - 32,
				100, 22, "Отмена", false, () -> this.minecraft.setScreen(this.parent)));
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
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, Theme.BACKDROP);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics, mouseX, mouseY, partialTick);
		Glass.panel(graphics, this.x0, this.y0, this.panelW, this.panelH, 10);

		int cx = this.x0 + this.panelW / 2;
		Glass.textCentered(graphics, "ЖДУ КЛАВИШУ", cx, this.y0 + 16, Theme.ACCENT);
		Glass.textCentered(graphics, this.prompt, cx, this.y0 + 38, Theme.TEXT);
		Glass.textCentered(graphics, "Клавиша или кнопка мыши · Esc — отмена", cx, this.y0 + 54, Theme.FAINT);

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
