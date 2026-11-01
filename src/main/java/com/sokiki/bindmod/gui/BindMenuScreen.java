package com.sokiki.bindmod.gui;

import com.sokiki.bindmod.Bind;
import com.sokiki.bindmod.BindKey;
import com.sokiki.bindmod.BindManager;
import com.sokiki.bindmod.BindMod;
import com.sokiki.bindmod.BindModConfig;
import com.sokiki.bindmod.Waypoint;
import com.sokiki.bindmod.Waypoints;
import com.sokiki.bindmod.ui.CycleRow;
import com.sokiki.bindmod.ui.Glass;
import com.sokiki.bindmod.ui.GlassButton;
import com.sokiki.bindmod.ui.SliderRow;
import com.sokiki.bindmod.ui.TabButton;
import com.sokiki.bindmod.ui.Theme;
import com.sokiki.bindmod.ui.ToggleRow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Главное меню мода (по правой Alt): вкладки, стеклянные панели, тумблеры.
 */
public class BindMenuScreen extends Screen {
	private static final String[] TABS = {"БИНДЫ", "МЕТКИ", "МОДУЛИ", "НАСТРОЙКИ"};
	private static final String[] CORNERS = {"Слева сверху", "Справа сверху", "Слева снизу", "Справа снизу"};
	private static final String[] AFK_MODES = {"Присесть", "Прыжок"};
	private static final int ROW_H = 24;
	private static final int ROW_STEP = 27;

	private final BindManager manager;
	private final int tab;

	private int x0;
	private int y0;
	private int panelW;
	private int panelH;

	private int listX;
	private int listY;
	private int listW;
	private int listH;
	private double scroll;
	private int maxScroll;
	private boolean confirmDelete;

	public BindMenuScreen(BindManager manager) {
		this(manager, 0);
	}

	public BindMenuScreen(BindManager manager, int tab) {
		super(Component.literal("Bind Mod"));
		this.manager = manager;
		this.tab = Mth.clamp(tab, 0, TABS.length - 1);
	}

	private BindModConfig config() {
		return this.manager.config();
	}

	private int itemCount() {
		return this.tab == 0 ? this.config().binds.size() : this.config().waypoints.size();
	}

	@Override
	protected void init() {
		this.clearWidgets();

		this.panelW = Math.min(580, this.width - 40);
		this.panelH = Math.min(336, this.height - 40);
		this.x0 = (this.width - this.panelW) / 2;
		this.y0 = (this.height - this.panelH) / 2;

		int sidebarX = this.x0 + 12;
		int sidebarY = this.y0 + 46;
		for (int i = 0; i < TABS.length; i++) {
			final int index = i;
			this.addRenderableWidget(new TabButton(sidebarX, sidebarY + i * 26, 108, 22, TABS[i], this.tab == i,
					() -> this.minecraft.setScreen(new BindMenuScreen(this.manager, index))));
		}

		int cx = this.x0 + 132;
		int cy = this.y0 + 44;
		int cw = this.panelW - 144;
		int ch = this.panelH - 56;

		switch (this.tab) {
			case 0 -> this.initListTab(cx, cy, cw, ch, true);
			case 1 -> this.initListTab(cx, cy, cw, ch, false);
			case 2 -> this.initModules(cx, cy, cw);
			default -> this.initSettings(cx, cy, cw);
		}
	}

	private void initListTab(int x, int y, int w, int h, boolean binds) {
		this.listX = x;
		this.listY = y + 18;
		this.listW = w;
		this.listH = h - 46;

		int contentHeight = this.itemCount() * ROW_STEP + 8;
		this.maxScroll = Math.max(0, contentHeight - this.listH);
		this.scroll = Mth.clamp(this.scroll, 0, this.maxScroll);

		int buttonY = y + h - 24;
		if (binds) {
			this.addRenderableWidget(new GlassButton(x, buttonY, 150, 22, "+  Добавить бинд", true,
					() -> this.minecraft.setScreen(new BindEditScreen(this, this.manager, null))));
		} else {
			this.addRenderableWidget(new GlassButton(x, buttonY, 110, 22, "+  Здесь", true, () -> {
				Waypoints.addHere(this.config());
				this.rebuildWidgets();
			}));
			this.addRenderableWidget(new GlassButton(x + 116, buttonY, 140, 22, "+  По координатам", false,
					() -> this.minecraft.setScreen(new WaypointEditScreen(this, this.manager, null))));
		}
	}

	private void initModules(int x, int y, int w) {
		BindModConfig cfg = this.config();
		int rowH = 22;
		int step = 27;
		int cy = y + 18;

		this.addRenderableWidget(new ToggleRow(x, cy, w, rowH, "Прочность брони (HUD)",
				() -> cfg.armorHud, value -> {
			cfg.armorHud = value;
			cfg.save();
		}));
		cy += step;

		this.addRenderableWidget(new CycleRow(x, cy, w, rowH, "Позиция HUD", CORNERS,
				() -> cfg.hudCorner, value -> {
			cfg.hudCorner = value;
			cfg.save();
		}));
		cy += step;

		this.addRenderableWidget(new ToggleRow(x, cy, w, rowH, "Метки в мире (3D)",
				() -> cfg.waypointsEnabled, value -> {
			cfg.waypointsEnabled = value;
			cfg.save();
		}));
		cy += step;

		this.addRenderableWidget(new ToggleRow(x, cy, w, rowH, "Список меток (HUD)",
				() -> cfg.waypointHud, value -> {
			cfg.waypointHud = value;
			cfg.save();
		}));
		cy += step;

		this.addRenderableWidget(new ToggleRow(x, cy, w, rowH, "Анти-АФК",
				() -> cfg.antiAfk, value -> {
			cfg.antiAfk = value;
			cfg.save();
		}));
		cy += step;

		this.addRenderableWidget(new SliderRow(x, cy, w, 26, "Интервал анти-АФК", 5, 300, 5, " с",
				() -> cfg.antiAfkInterval, value -> {
			cfg.antiAfkInterval = value;
			cfg.save();
		}));
		cy += 32;

		this.addRenderableWidget(new CycleRow(x, cy, w, rowH, "Действие анти-АФК", AFK_MODES,
				() -> cfg.antiAfkMode, value -> {
			cfg.antiAfkMode = value;
			cfg.save();
		}));
	}

	private void initSettings(int x, int y, int w) {
		int rowH = 22;
		int cy = y + 18;

		this.addRenderableWidget(new GlassButton(x, cy, w, rowH,
				"Клавиша меню:  " + this.config().menuKey.displayName(), false,
				() -> this.minecraft.setScreen(new KeyCaptureScreen(this, "Новая клавиша для меню", key -> {
					this.config().menuKey = key;
					this.config().save();
					this.manager.latchMenuKey();
					this.minecraft.setScreen(new BindMenuScreen(this.manager, 3));
				}))));
		cy += 27;

		this.addRenderableWidget(new GlassButton(x, cy, w, rowH,
				"Клавиша метки:  " + this.config().waypointKey.displayName(), false,
				() -> this.minecraft.setScreen(new KeyCaptureScreen(this, "Клавиша быстрой метки", key -> {
					this.config().waypointKey = key;
					this.config().save();
					this.manager.latchMenuKey();
					this.minecraft.setScreen(new BindMenuScreen(this.manager, 3));
				}))));
		cy += 34;

		this.addRenderableWidget(new GlassButton(x, cy, w, rowH, "Удалить все бинды", false, () -> {
			if (!this.confirmDelete) {
				this.confirmDelete = true;
				return;
			}
			this.config().binds.clear();
			this.config().save();
			this.confirmDelete = false;
			this.rebuildWidgets();
		}));
		cy += 27;

		this.addRenderableWidget(new GlassButton(x, cy, w, rowH, "Удалить все метки", false, () -> {
			if (!this.confirmDelete) {
				this.confirmDelete = true;
				return;
			}
			this.config().waypoints.clear();
			this.config().save();
			this.confirmDelete = false;
			this.rebuildWidgets();
		}));
	}

	private boolean inList(double mouseX, double mouseY) {
		return mouseX >= this.listX && mouseX <= this.listX + this.listW
				&& mouseY >= this.listY && mouseY <= this.listY + this.listH;
	}

	private int rowIndexAt(double mouseX, double mouseY) {
		if ((this.tab != 0 && this.tab != 1) || !this.inList(mouseX, mouseY)) {
			return -1;
		}
		int relative = (int) (mouseY - this.listY - 4 + this.scroll);
		int index = relative / ROW_STEP;
		if (index < 0 || index >= this.itemCount()) {
			return -1;
		}
		int rowY = this.listY + 4 + index * ROW_STEP - (int) this.scroll;
		if (mouseY < rowY || mouseY > rowY + ROW_H) {
			return -1;
		}
		return index;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && (this.tab == 0 || this.tab == 1)) {
			int index = this.rowIndexAt(mouseX, mouseY);
			if (index >= 0) {
				boolean deleteZone = mouseX >= this.listX + this.listW - 28;
				if (this.tab == 0) {
					List<Bind> binds = this.config().binds;
					if (deleteZone) {
						binds.remove(index);
						this.config().save();
						this.rebuildWidgets();
					} else {
						this.minecraft.setScreen(new BindEditScreen(this, this.manager, binds.get(index)));
					}
				} else {
					List<Waypoint> waypoints = this.config().waypoints;
					if (deleteZone) {
						waypoints.remove(index);
						this.config().save();
						this.rebuildWidgets();
					} else {
						this.minecraft.setScreen(new WaypointEditScreen(this, this.manager, waypoints.get(index)));
					}
				}
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if ((this.tab == 0 || this.tab == 1) && this.inList(mouseX, mouseY) && this.maxScroll > 0) {
			this.scroll = Mth.clamp(this.scroll - scrollY * 18.0, 0, this.maxScroll);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		BindKey menuKey = this.config().menuKey;
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
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, Theme.BACKDROP);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics, mouseX, mouseY, partialTick);
		Glass.panel(graphics, this.x0, this.y0, this.panelW, this.panelH, 10);

		Glass.text(graphics, "bindmod", this.x0 + 14, this.y0 + 14, Theme.ACCENT, true);
		String right = this.config().menuKey.displayName() + "  ·  v" + BindMod.VERSION;
		Glass.textRight(graphics, right, this.x0 + this.panelW - 14, this.y0 + 14, Theme.FAINT);
		graphics.fill(this.x0 + 12, this.y0 + 34, this.x0 + this.panelW - 12, this.y0 + 35, Theme.BORDER_SOFT);

		int cx = this.x0 + 132;
		int cy = this.y0 + 44;
		int cw = this.panelW - 144;
		int ch = this.panelH - 56;

		switch (this.tab) {
			case 0 -> this.renderBinds(graphics, cx, cy, cw, ch, mouseX, mouseY);
			case 1 -> this.renderWaypoints(graphics, cx, cy, cw, ch, mouseX, mouseY);
			case 2 -> this.renderModules(graphics, cx, cy, cw, ch);
			default -> this.renderSettings(graphics, cx, cy, cw, ch);
		}

		super.render(graphics, mouseX, mouseY, partialTick);

		Glass.text(graphics, "Esc — закрыть", this.x0 + 14, this.y0 + this.panelH - 18, Theme.FAINT);
	}

	private void renderBinds(GuiGraphics graphics, int x, int y, int w, int h, int mouseX, int mouseY) {
		List<Bind> binds = this.config().binds;
		Glass.text(graphics, "БИНДЫ", x, y, Theme.MUTED);
		Glass.textRight(graphics, binds.size() + " шт.", x + w, y, Theme.FAINT);

		Glass.inner(graphics, x, y + 18, w, h - 46, 6);
		graphics.enableScissor(x, y + 18, x + w, y + 18 + h - 46);
		if (binds.isEmpty()) {
			Glass.textCentered(graphics, "Пока пусто — добавьте первый бинд",
					x + w / 2, y + 18 + (h - 46) / 2 - 4, Theme.FAINT);
		}
		int hoverIndex = this.rowIndexAt(mouseX, mouseY);
		for (int i = 0; i < binds.size(); i++) {
			Bind bind = binds.get(i);
			int rowY = this.listY + 4 + i * ROW_STEP - (int) this.scroll;
			if (rowY + ROW_H < this.listY || rowY > this.listY + this.listH) {
				continue;
			}
			boolean hover = hoverIndex == i;
			Glass.round(graphics, x + 4, rowY, w - 8, ROW_H, 5, hover ? Theme.ROW_HOVER : Theme.ROW);

			String keyName = bind.key.displayName();
			int chipW = Math.max(38, Glass.textWidth(keyName) + 14);
			Glass.round(graphics, x + 8, rowY + 4, chipW, 16, 4, 0x2E64E1FF);
			Glass.textCentered(graphics, keyName, x + 8 + chipW / 2, rowY + 8, Theme.ACCENT);

			int commandX = x + 8 + chipW + 10;
			int commandWidth = w - (commandX - x) - 40;
			Glass.text(graphics, bind.shortCommand(Math.max(8, commandWidth / 6)), commandX, rowY + 8, Theme.TEXT);

			int crossColor = hover && mouseX >= this.listX + this.listW - 28 ? Theme.DANGER : Theme.FAINT;
			Glass.text(graphics, "×", x + w - 22, rowY + 7, crossColor);
		}
		graphics.disableScissor();
		this.renderScrollbar(graphics, x, y, w, h);
	}

	private void renderWaypoints(GuiGraphics graphics, int x, int y, int w, int h, int mouseX, int mouseY) {
		List<Waypoint> waypoints = this.config().waypoints;
		Glass.text(graphics, "МЕТКИ", x, y, Theme.MUTED);
		Glass.textRight(graphics, "клавиша: " + this.config().waypointKey.displayName(), x + w, y, Theme.FAINT);

		Glass.inner(graphics, x, y + 18, w, h - 46, 6);
		graphics.enableScissor(x, y + 18, x + w, y + 18 + h - 46);
		if (waypoints.isEmpty()) {
			Glass.textCentered(graphics, "Меток нет — «+ Здесь» поставит метку у вас",
					x + w / 2, y + 18 + (h - 46) / 2 - 10, Theme.FAINT);
			Glass.textCentered(graphics, "или добавьте метку по координатам",
					x + w / 2, y + 18 + (h - 46) / 2 + 4, Theme.FAINT);
		}
		int hoverIndex = this.rowIndexAt(mouseX, mouseY);
		for (int i = 0; i < waypoints.size(); i++) {
			Waypoint waypoint = waypoints.get(i);
			int rowY = this.listY + 4 + i * ROW_STEP - (int) this.scroll;
			if (rowY + ROW_H < this.listY || rowY > this.listY + this.listH) {
				continue;
			}
			boolean hover = hoverIndex == i;
			Glass.round(graphics, x + 4, rowY, w - 8, ROW_H, 5, hover ? Theme.ROW_HOVER : Theme.ROW);

			int rgb = waypoint.rgb() | 0xFF000000;
			Glass.round(graphics, x + 8, rowY + 8, 8, 8, 2, rgb);

			String name = waypoint.name == null || waypoint.name.isBlank() ? "Метка" : waypoint.name;
			Glass.text(graphics, name, x + 22, rowY + 5, Theme.TEXT);
			Glass.text(graphics, waypoint.coords(), x + 22, rowY + 14, Theme.FAINT);

			String distance = Math.round(Waypoints.distanceTo(waypoint)) + " м";
			Glass.textRight(graphics, distance, x + w - 34, rowY + 8, Theme.ACCENT);

			int crossColor = hover && mouseX >= this.listX + this.listW - 28 ? Theme.DANGER : Theme.FAINT;
			Glass.text(graphics, "×", x + w - 22, rowY + 7, crossColor);
		}
		graphics.disableScissor();
		this.renderScrollbar(graphics, x, y, w, h);
	}

	private void renderScrollbar(GuiGraphics graphics, int x, int y, int w, int h) {
		if (this.maxScroll <= 0) {
			return;
		}
		int trackX = x + w - 3;
		int trackH = h - 46;
		Glass.round(graphics, trackX, y + 18, 3, trackH, 1, 0x22FFFFFF);
		int thumbH = Math.max(16, (int) (trackH * (trackH / (double) (trackH + this.maxScroll))));
		int thumbY = y + 18 + (int) ((trackH - thumbH) * (this.scroll / this.maxScroll));
		Glass.round(graphics, trackX, thumbY, 3, thumbH, 1, Theme.ACCENT_SOFT);
	}

	private void renderModules(GuiGraphics graphics, int x, int y, int w, int h) {
		Glass.text(graphics, "МОДУЛИ", x, y, Theme.MUTED);
		Glass.text(graphics, "Всё работает только у вас в клиенте — на сервер ничего не ставится.",
				x, y + h - 32, Theme.FAINT);
		Glass.text(graphics, "Метки видны в мире (3D) и в списке HUD, первыми — ближайшие.",
				x, y + h - 20, Theme.FAINT);
	}

	private void renderSettings(GuiGraphics graphics, int x, int y, int w, int h) {
		Glass.text(graphics, "НАСТРОЙКИ", x, y, Theme.MUTED);
		if (this.confirmDelete) {
			Glass.text(graphics, "Нажмите ту же кнопку ещё раз для подтверждения",
					x, y + 150, Theme.DANGER);
		}
		Glass.text(graphics, "Конфиг: .minecraft/config/bindmod.json", x, y + h - 32, Theme.FAINT);
		Glass.text(graphics, "Бинды, метки, HUD и анти-АФК сохраняются автоматически.",
				x, y + h - 20, Theme.FAINT);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
