package com.sokiki.bindmod;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

/**
 * Анти-АФК: раз в N секунд очень коротко нажимает «присесть» или «прыжок».
 * Игра сама отправляет пакет движения, поэтому сервер не считает игрока AFK.
 * Никаких читерских функций — обычное нажатие клавиши, как будто игрок сам пошевелился.
 */
public final class AntiAfk {
	private int cooldown;
	private int holdTicks;
	private KeyMapping pressed;

	public void tick(BindModConfig config) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.getConnection() == null || mc.screen != null || !config.antiAfk) {
			this.release();
			this.cooldown = 0;
			return;
		}

		if (this.holdTicks > 0) {
			this.holdTicks--;
			if (this.holdTicks == 0) {
				this.release();
			}
			return;
		}

		this.cooldown++;
		int intervalTicks = Math.max(5, config.antiAfkInterval) * 20;
		if (this.cooldown < intervalTicks) {
			return;
		}

		this.cooldown = 0;
		KeyMapping key = config.antiAfkMode == BindModConfig.MODE_JUMP
				? mc.options.keyJump
				: mc.options.keyShift;
		key.setDown(true);
		this.pressed = key;
		this.holdTicks = 2;
		mc.player.swing(InteractionHand.MAIN_HAND);
	}

	private void release() {
		if (this.pressed != null) {
			this.pressed.setDown(false);
			this.pressed = null;
		}
		this.holdTicks = 0;
	}
}
