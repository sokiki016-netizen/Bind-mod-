package com.sokiki.bindmod;

import com.sokiki.bindmod.gui.BindMenuScreen;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Следит за нажатиями клавиш и выполняет бинды.
 */
public final class BindManager {
	private final BindModConfig config;
	private final Set<String> held = new HashSet<>();
	private boolean menuLatched;

	public BindManager(BindModConfig config) {
		this.config = config;
	}

	public BindModConfig config() {
		return this.config;
	}

	/**
	 * Вызывается, когда меню закрыли той же клавишей, которой открыли:
	 * не даём меню открыться повторно, пока клавиша не отпущена.
	 */
	public void latchMenuKey() {
		this.menuLatched = true;
	}

	public void onEndClientTick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc == null || mc.getWindow() == null) {
			return;
		}

		boolean inGame = mc.screen == null && mc.player != null && mc.getConnection() != null;
		BindKey menuKey = this.config.menuKey;

		boolean menuDown = menuKey != null && menuKey.isDown();
		boolean menuWas = this.isHeld(menuKey);
		this.setHeld(menuKey, menuDown);

		if (menuDown && !menuWas && inGame && !this.menuLatched) {
			this.menuLatched = true;
			mc.setScreen(new BindMenuScreen(this));
			return;
		}
		if (!menuDown) {
			this.menuLatched = false;
		}

		// быстрая метка на текущих координатах
		BindKey waypointKey = this.config.waypointKey;
		boolean placeDown = waypointKey != null && waypointKey.isSet() && waypointKey.isDown();
		boolean placeWas = this.isHeld(waypointKey);
		this.setHeld(waypointKey, placeDown);
		if (placeDown && !placeWas && inGame && !waypointKey.sameAs(menuKey)) {
			Waypoint placed = Waypoints.addHere(this.config);
			if (placed != null) {
				Waypoints.notifyPlayer("§b[Bind Mod] §fМетка поставлена: §b" + placed.name
						+ " §7(" + placed.coords() + ")");
			}
		}

		List<Bind> snapshot = new ArrayList<>(this.config.binds);
		for (Bind bind : snapshot) {
			BindKey key = bind.key;
			if (key == null || !key.isSet()) {
				continue;
			}
			boolean down = key.isDown();
			boolean was = this.isHeld(key);
			this.setHeld(key, down);
			if (down && !was && inGame) {
				this.run(bind.command);
			}
		}
	}

	private void run(String rawCommand) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.getConnection() == null || rawCommand == null) {
			return;
		}
		String command = rawCommand.trim();
		if (command.isEmpty()) {
			return;
		}
		try {
			if (command.startsWith("/")) {
				String bare = command.substring(1).trim();
				if (!bare.isEmpty()) {
					mc.getConnection().sendCommand(bare);
				}
			} else {
				mc.getConnection().sendChat(command);
			}
		} catch (Exception e) {
			BindMod.LOGGER.error("[Bind Mod] Не удалось выполнить '{}'", command, e);
		}
	}

	private String id(BindKey key) {
		return key == null ? "none" : key.serialize();
	}

	private boolean isHeld(BindKey key) {
		return this.held.contains(this.id(key));
	}

	private void setHeld(BindKey key, boolean down) {
		String id = this.id(key);
		if (down) {
			this.held.add(id);
		} else {
			this.held.remove(id);
		}
	}
}
