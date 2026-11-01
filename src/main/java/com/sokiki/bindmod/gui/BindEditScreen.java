package com.sokiki.bindmod.gui;

import com.sokiki.bindmod.Bind;
import com.sokiki.bindmod.BindKey;
import com.sokiki.bindmod.BindManager;
import com.sokiki.bindmod.ui.Glass;
import com.sokiki.bindmod.ui.GlassButton;
import com.sokiki.bindmod.ui.Theme;
import net.minecraft.client.gui.GuiGraphics;
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
	private String hint = "";
	private int x0;
	private int y0;
	private int panelW = 340;
	private int panelH = 178;

	public BindEditScreen(Screen parent, BindManager manager, Bind existing) {
		super(Component.literal("Bind Mod"));
		this.parent = parent;
		this.manager = manager;
		this.existing = existing;
	}

	@Override
	protected void init() {
		this.clearWidgets();
		this.panelW = Math.min(340, this.width - 40);
		this.panelH = Math.min(196, this.height - 40);
		this.x0 = (this.width - this.panelW) / 2;
		this.y0 = (this.height - this.panelH) / 2;

		int fieldX = this.x0 + 16;
		int fieldW = this.panelW - 32;

		this.commandBox = new EditBox(this.font, fieldX + 7, this.y0 + 56, fieldW - 14, 16,
				Component.literal("Команда"));
		this.commandBox.setMaxLength(256);
		this.commandBox.setBordered(false);
		this.commandBox.setTextColor(Theme.TEXT);
		this.commandBox.setTextColorUneditable(Theme.MUTED);
		this.commandBox.setValue(this.existing == null ? "/" : this.existing.command);
		this.commandBox.setFocused(true);
		this.setFocused(this.commandBox);
		this.addRenderableWidget(this.commandBox);

		int buttonY = this.y0 + this.panelH - 32;
		if (this.existing != null) {
			this.addRenderableWidget(new GlassButton(fieldX, buttonY, fieldW / 2 - 4, 22, "Удалить", false, () -> {
				this.manager.config().binds.remove(this.existing);
				this.manager.config().save();
				this.minecraft.setScreen(this.parent);
			}));
			this.addRenderableWidget(new GlassButton(fieldX + fieldW / 2 + 4, buttonY, fieldW / 2 - 4, 22,
					"Далее: клавиша", true, this::proceed));
		} else {
			this.addRenderableWidget(new GlassButton(fieldX, buttonY, fieldW / 2 - 4, 22, "Отмена", false,
					() -> this.minecraft.setScreen(this.parent)));
			this.addRenderableWidget(new GlassButton(fieldX + fieldW / 2 + 4, buttonY, fieldW / 2 - 4, 22,
					"Далее: клавиша", true, this::proceed));
		}
	}

	private void proceed() {
		String command = this.commandBox.getValue().trim();
		if (command.isEmpty()) {
			this.hint = "Введите команду или текст сообщения";
			return;
		}
		this.minecraft.setScreen(new KeyCaptureScreen(this, "Клавиша для «" + shortCommand(command) + "»",
				key -> this.apply(key, command)));
	}

	private static String shortCommand(String command) {
		return command.length() > 18 ? command.substring(0, 17) + "…" : command;
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
					? "§b[Bind Mod] §fКлавиша была занята — старый бинд заменён на §b" + command
					: "§b[Bind Mod] §fЗабинжено: §f" + key.displayName() + " §7→ §b" + command;
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
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, Theme.BACKDROP);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics, mouseX, mouseY, partialTick);
		Glass.panel(graphics, this.x0, this.y0, this.panelW, this.panelH, 10);

		int fieldX = this.x0 + 16;
		int fieldW = this.panelW - 32;

		Glass.text(graphics, this.existing == null ? "НОВЫЙ БИНД" : "ИЗМЕНИТЬ БИНД",
				fieldX, this.y0 + 18, Theme.MUTED);
		Glass.textRight(graphics, "Esc — назад", this.x0 + this.panelW - 16, this.y0 + 18, Theme.FAINT);

		Glass.text(graphics, "Команда", fieldX, this.y0 + 40, Theme.TEXT);
		Glass.inner(graphics, fieldX, this.y0 + 51, fieldW, 24, 4);

		if (this.existing != null && this.existing.key != null) {
			Glass.text(graphics, "Клавиша: " + this.existing.key.displayName(),
					fieldX, this.y0 + 84, Theme.MUTED);
		} else {
			Glass.text(graphics, "С «/» — команда, без «/» — сообщение в чат",
					fieldX, this.y0 + 84, Theme.FAINT);
		}

		if (!this.hint.isEmpty()) {
			Glass.text(graphics, this.hint, fieldX, this.y0 + 100, Theme.DANGER);
		}

		Glass.text(graphics, "Enter — далее по клавише", fieldX, this.y0 + this.panelH - 46, Theme.FAINT);

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
