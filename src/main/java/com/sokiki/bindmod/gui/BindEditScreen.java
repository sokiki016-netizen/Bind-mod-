package com.sokiki.bindmod.gui;

import com.sokiki.bindmod.Bind;
import com.sokiki.bindmod.BindKey;
import com.sokiki.bindmod.BindManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BindEditScreen extends Screen {
	private final Screen parent;
	private final BindManager manager;
	private final Bind existing;

	private EditBox commandBox;
	private String hint;

	public BindEditScreen(Screen parent, BindManager manager, Bind existing) {
		super(Component.literal("Bind Mod"));
		this.parent = parent;
		this.manager = manager;
		this.existing = existing;
		this.hint = "";
	}

	@Override
	protected void init() {
		this.clearWidgets();
		int cx = this.width / 2;

		this.commandBox = new EditBox(this.font, cx - 150, 84, 300, 20, Component.literal("Команда"));
		this.commandBox.setMaxLength(256);
		this.commandBox.setValue(this.existing == null ? "/" : this.existing.command);
		this.commandBox.setFocused(true);
		this.addRenderableWidget(this.commandBox);

		this.addRenderableWidget(Button.builder(Component.literal("§aДалее: нажать клавишу"), b -> this.proceed())
				.bounds(cx - 150, 116, 300, 20).build());

		if (this.existing != null) {
			this.addRenderableWidget(Button.builder(Component.literal("§cУдалить бинд"), b -> {
				this.manager.config().binds.remove(this.existing);
				this.manager.config().save();
				this.minecraft.setScreen(this.parent);
			}).bounds(cx - 150, 140, 146, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Отмена"), b -> this.minecraft.setScreen(this.parent))
					.bounds(cx + 4, 140, 146, 20).build());
		} else {
			this.addRenderableWidget(Button.builder(Component.literal("Отмена"), b -> this.minecraft.setScreen(this.parent))
					.bounds(cx - 150, 140, 300, 20).build());
		}
	}

	private void proceed() {
		String command = this.commandBox.getValue().trim();
		if (command.isEmpty()) {
			this.hint = "§cВведите команду или текст сообщения";
			return;
		}
		this.minecraft.setScreen(new KeyCaptureScreen(this, "Клавиша для команды " + shortCommand(command),
				key -> this.apply(key, command)));
	}

	private static String shortCommand(String command) {
		return command.length() > 20 ? command.substring(0, 19) + "…" : command;
	}

	private void apply(BindKey key, String command) {
		List<Bind> binds = this.manager.config().binds;
		boolean replaced = binds.removeIf(other -> other != this.existing && other.key != null && other.key.sameAs(key));

		if (this.existing == null) {
			binds.add(new Bind(key, command));
		} else {
			this.existing.key = key;
			this.existing.command = command;
		}
		this.manager.config().save();

		if (this.minecraft.player != null) {
			String message = replaced
					? "§e[Bind Mod] §fКлавиша была занята — старый бинд заменён: §a" + command
					: "§a[Bind Mod] §fЗабинжено: §e" + key.displayName() + " §7→ §a" + command;
			this.minecraft.player.displayClientMessage(Component.literal(message), false);
		}
		this.minecraft.setScreen(null);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.proceed();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.minecraft.setScreen(this.parent);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, 0xE0101010);
		super.render(graphics, mouseX, mouseY, partialTick);
		int cx = this.width / 2;

		graphics.drawCenteredString(this.font,
				Component.literal(this.existing == null ? "§l§bНовый бинд" : "§l§bИзменить бинд"),
				cx, 34, 0xFFFFFF);

		graphics.drawCenteredString(this.font,
				Component.literal("§7Команда (начните с §f/§7 для команд, без §f/§7 — сообщение в чат):"),
				cx, 68, 0xA0A0A0);

		if (this.existing != null && this.existing.key != null) {
			graphics.drawCenteredString(this.font,
					Component.literal("§7Текущая клавиша: §f" + this.existing.key.displayName()),
					cx, 168, 0xA0A0A0);
		}

		if (!this.hint.isEmpty()) {
			graphics.drawCenteredString(this.font, Component.literal(this.hint), cx, 186, 0xFF5555);
		}

		if (this.existing != null && this.existing.key != null && this.existing.key.isMouse()) {
			graphics.drawCenteredString(this.font, Component.literal("§8(кнопка мыши)"), cx, 200, 0x707070);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
