package com.sokiki.bindmod.gui;

import com.sokiki.bindmod.BindManager;
import com.sokiki.bindmod.BindModConfig;
import com.sokiki.bindmod.Waypoint;
import com.sokiki.bindmod.Waypoints;
import com.sokiki.bindmod.ui.Glass;
import com.sokiki.bindmod.ui.GlassButton;
import com.sokiki.bindmod.ui.Theme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class WaypointEditScreen extends Screen {
	private final Screen parent;
	private final BindManager manager;
	private final Waypoint existing;

	private EditBox nameBox;
	private EditBox xBox;
	private EditBox yBox;
	private EditBox zBox;
	private String hint = "";

	private int x0;
	private int y0;
	private int panelW = 340;
	private int panelH = 236;

	public WaypointEditScreen(Screen parent, BindManager manager, Waypoint existing) {
		super(Component.literal("Bind Mod"));
		this.parent = parent;
		this.manager = manager;
		this.existing = existing;
	}

	private BindModConfig config() {
		return this.manager.config();
	}

	@Override
	protected void init() {
		this.clearWidgets();
		this.panelW = Math.min(340, this.width - 40);
		this.panelH = Math.min(252, this.height - 40);
		this.x0 = (this.width - this.panelW) / 2;
		this.y0 = (this.height - this.panelH) / 2;

		boolean hasExisting = this.existing != null;
		double px = this.minecraft.player != null ? this.minecraft.player.getX() : 0;
		double py = this.minecraft.player != null ? this.minecraft.player.getY() : 64;
		double pz = this.minecraft.player != null ? this.minecraft.player.getZ() : 0;

		int fieldX = this.x0 + 16;
		int fieldW = this.panelW - 32;

		this.nameBox = new EditBox(this.font, fieldX + 7, this.y0 + 44, fieldW - 14, 16, Component.literal("Имя"));
		this.nameBox.setMaxLength(32);
		this.nameBox.setBordered(false);
		this.nameBox.setValue(hasExisting ? this.existing.name : Waypoints.nextName(this.config()));
		this.addRenderableWidget(this.nameBox);

		int coordW = (fieldW - 16) / 3;
		this.xBox = this.coordBox(fieldX, this.y0 + 92, coordW, hasExisting ? this.existing.x : px);
		this.yBox = this.coordBox(fieldX + coordW + 8, this.y0 + 92, coordW, hasExisting ? this.existing.y : py);
		this.zBox = this.coordBox(fieldX + (coordW + 8) * 2, this.y0 + 92, coordW, hasExisting ? this.existing.z : pz);

		this.setFocused(this.nameBox);
		this.nameBox.setFocused(true);

		int buttonY = this.y0 + this.panelH - 32;
		this.addRenderableWidget(new GlassButton(fieldX, buttonY, 96, 22, "Здесь", false, () -> this.fillFromPlayer()));
		this.addRenderableWidget(new GlassButton(fieldX + 102, buttonY, fieldW - 102, 22, "Сохранить", true, this::save));
	}

	private EditBox coordBox(int x, int y, int w, double value) {
		EditBox box = new EditBox(this.font, x + 15, y + 5, w - 21, 16, Component.literal("coord"));
		box.setMaxLength(18);
		box.setBordered(false);
		box.setValue(trimNumber(value));
		this.addRenderableWidget(box);
		return box;
	}

	private static String trimNumber(double value) {
		String text = String.format(java.util.Locale.ROOT, "%.1f", value);
		return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
	}

	private void fillFromPlayer() {
		if (this.minecraft.player == null) {
			return;
		}
		this.xBox.setValue(trimNumber(this.minecraft.player.getX()));
		this.yBox.setValue(trimNumber(this.minecraft.player.getY()));
		this.zBox.setValue(trimNumber(this.minecraft.player.getZ()));
		this.hint = "";
	}

	private double parse(EditBox box, double fallback) {
		try {
			return Double.parseDouble(box.getValue().trim().replace(',', '.'));
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private void save() {
		double px = this.minecraft.player != null ? this.minecraft.player.getX() : 0;
		double py = this.minecraft.player != null ? this.minecraft.player.getY() : 64;
		double pz = this.minecraft.player != null ? this.minecraft.player.getZ() : 0;

		double x = this.parse(this.xBox, px);
		double y = this.parse(this.yBox, py);
		double z = this.parse(this.zBox, pz);

		String name = this.nameBox.getValue().trim();
		if (name.isEmpty()) {
			name = "Метка";
		}

		if (this.existing == null) {
			Waypoint waypoint = new Waypoint(name, x, y, z, this.config().waypoints.size(),
					Waypoints.currentDimension());
			this.config().waypoints.add(waypoint);
		} else {
			this.existing.name = name;
			this.existing.x = x;
			this.existing.y = y;
			this.existing.z = z;
		}
		this.config().save();
		Waypoints.notifyPlayer("§b[Bind Mod] §fМетка сохранена: §b" + name + " §7("
				+ Math.round(x) + " " + Math.round(y) + " " + Math.round(z) + ")");
		this.minecraft.setScreen(this.parent);
	}

	private void cycleColor() {
		if (this.existing != null) {
			this.existing.color = this.existing.nextColor();
			this.config().save();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.save();
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

		Glass.text(graphics, this.existing == null ? "НОВАЯ МЕТКА" : "ИЗМЕНИТЬ МЕТКУ",
				fieldX, this.y0 + 16, Theme.MUTED);
		Glass.textRight(graphics, "Esc — назад", this.x0 + this.panelW - 16, this.y0 + 16, Theme.FAINT);

		Glass.text(graphics, "Название", fieldX, this.y0 + 32, Theme.TEXT);
		Glass.inner(graphics, fieldX, this.y0 + 39, fieldW, 24, 4);

		int coordW = (fieldW - 16) / 3;
		Glass.text(graphics, "Координаты  §8X · Y · Z", fieldX, this.y0 + 70, Theme.TEXT);
		Glass.inner(graphics, fieldX, this.y0 + 86, coordW, 26, 4);
		Glass.inner(graphics, fieldX + coordW + 8, this.y0 + 86, coordW, 26, 4);
		Glass.inner(graphics, fieldX + (coordW + 8) * 2, this.y0 + 86, coordW, 26, 4);
		Glass.text(graphics, "X", fieldX + 5, this.y0 + 95, Theme.FAINT);
		Glass.text(graphics, "Y", fieldX + coordW + 13, this.y0 + 95, Theme.FAINT);
		Glass.text(graphics, "Z", fieldX + (coordW + 8) * 2 + 13, this.y0 + 95, Theme.FAINT);

		if (this.existing != null) {
			int rgb = Waypoint.rgbOf(this.existing.color);
			Glass.text(graphics, "Цвет", fieldX, this.y0 + 124, Theme.TEXT);
			int bx = fieldX + 34;
			boolean hover = mouseX >= bx && mouseX <= bx + 60 && mouseY >= this.y0 + 120 && mouseY <= this.y0 + 136;
			Glass.round(graphics, bx, this.y0 + 121, 60, 13, 4, (rgb & 0x00FFFFFF) | 0xCC000000);
			Glass.round(graphics, bx, this.y0 + 121, 60, 13, 4, (rgb & 0x00FFFFFF) | (hover ? 0x66000000 : 0x33000000));
			Glass.text(graphics, hover ? "сменить ›" : "›", bx + 62, this.y0 + 124, Theme.FAINT);
		}

		if (!this.hint.isEmpty()) {
			Glass.text(graphics, this.hint, fieldX, this.y0 + 144, Theme.DANGER);
		}

		Glass.text(graphics, "«Здесь» подставит ваши текущие координаты", fieldX,
				this.y0 + this.panelH - 46, Theme.FAINT);

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.existing != null) {
			int fieldX = this.x0 + 16;
			int bx = fieldX + 34;
			if (button == 0 && mouseX >= bx && mouseX <= bx + 60
					&& mouseY >= this.y0 + 120 && mouseY <= this.y0 + 136) {
				this.cycleColor();
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
