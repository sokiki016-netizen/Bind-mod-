package com.sokiki.bindmod.gui;

import com.sokiki.bindmod.Bind;
import com.sokiki.bindmod.BindKey;
import com.sokiki.bindmod.BindManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BindMenuScreen extends Screen {
	private static final int PER_PAGE = 6;
	private static final int ROW_H = 26;

	private final BindManager manager;
	private int page;

	public BindMenuScreen(BindManager manager) {
		super(Component.literal("Bind Mod"));
		this.manager = manager;
		this.page = 0;
	}

	@Override
	protected void init() {
		this.clearWidgets();
		List<Bind> binds = this.manager.config().binds;
		int cx = this.width / 2;
		int top = 58;
		int start = this.page * PER_PAGE;

		for (int i = 0; i < PER_PAGE; i++) {
			int index = start + i;
			if (index >= binds.size()) {
				break;
			}
			Bind bind = binds.get(index);
			int y = top + i * ROW_H;

			this.addRenderableWidget(Button.builder(
							Component.literal("[" + bind.key.displayName() + "]"),
							b -> this.minecraft.setScreen(new BindEditScreen(this, this.manager, bind)))
					.bounds(cx - 172, y, 84, 20).build());

			this.addRenderableWidget(Button.builder(
							Component.literal(bind.shortCommand(22)),
							b -> this.minecraft.setScreen(new BindEditScreen(this, this.manager, bind)))
					.bounds(cx - 84, y, 190, 20).build());

			this.addRenderableWidget(Button.builder(
							Component.literal("§cX"),
							b -> {
								this.manager.config().binds.remove(bind);
								this.manager.config().save();
								this.page = Math.max(0, Math.min(this.page, this.maxPage()));
								this.rebuild();
							})
					.bounds(cx + 110, y, 24, 20).build());
		}

		int pageTop = top + PER_PAGE * ROW_H + 2;
		Button prev = Button.builder(Component.literal("§l<"), b -> {
			this.page = Math.max(0, this.page - 1);
			this.rebuild();
		}).bounds(cx - 60, pageTop, 40, 18).build();
		Button next = Button.builder(Component.literal("§l>"), b -> {
			this.page = Math.min(this.maxPage(), this.page + 1);
			this.rebuild();
		}).bounds(cx + 20, pageTop, 40, 18).build();
		prev.active = this.page > 0;
		next.active = this.page < this.maxPage();
		this.addRenderableWidget(prev);
		this.addRenderableWidget(next);

		int bottom = this.height - 46;
		this.addRenderableWidget(Button.builder(Component.literal("§a+ Добавить бинд"), b ->
						this.minecraft.setScreen(new BindEditScreen(this, this.manager, null)))
				.bounds(cx - 100, bottom, 200, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("§7Клавиша меню: §f" + this.manager.config().menuKey.displayName()), b ->
						this.minecraft.setScreen(new KeyCaptureScreen(this, "Нажмите новую клавишу для меню", key -> {
							this.manager.config().menuKey = key;
							this.manager.config().save();
							this.manager.latchMenuKey();
							this.minecraft.setScreen(new BindMenuScreen(this.manager));
						})))
				.bounds(cx - 100, this.height - 70, 200, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Готово"), b -> this.onClose())
				.bounds(cx - 100, this.height - 22, 200, 20).build());
	}

	private int maxPage() {
		int size = this.manager.config().binds.size();
		if (size <= PER_PAGE) {
			return 0;
		}
		return (size - 1) / PER_PAGE;
	}

	private void rebuild() {
		this.clearWidgets();
		this.init();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		BindKey menuKey = this.manager.config().menuKey;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE
				|| (menuKey != null && !menuKey.isMouse() && menuKey.code == keyCode)) {
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void onClose() {
		this.manager.latchMenuKey();
		this.minecraft.setScreen(null);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, 0xC8101010);
		super.render(graphics, mouseX, mouseY, partialTick);

		List<Bind> binds = this.manager.config().binds;
		int cx = this.width / 2;

		graphics.drawCenteredString(this.font, Component.literal("§l§bBind Mod §r§7— бинды команд"), cx, 18, 0xFFFFFF);
		graphics.drawCenteredString(this.font,
				Component.literal("§7Биндов: §f" + binds.size() + "   §7Страница: §f" + (this.page + 1) + "/" + (this.maxPage() + 1)),
				cx, 36, 0xA0A0A0);

		if (binds.isEmpty()) {
			graphics.drawCenteredString(this.font, Component.literal("§7Биндов пока нет."), cx, 70, 0xA0A0A0);
			graphics.drawCenteredString(this.font, Component.literal("§7Нажмите «Добавить бинд», введите команду"), cx, 86, 0xA0A0A0);
			graphics.drawCenteredString(this.font, Component.literal("§7и нажмите любую клавишу."), cx, 100, 0xA0A0A0);
		}

		graphics.drawCenteredString(this.font,
				Component.literal("§8Клавиша меню: §7" + this.manager.config().menuKey.displayName() + " §8| Esc — закрыть"),
				cx, this.height - 84, 0x707070);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
