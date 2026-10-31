package com.sokiki.bindmod;

import com.sokiki.bindmod.hud.ArmorHud;
import com.sokiki.bindmod.hud.WaypointHud;
import com.sokiki.bindmod.hud.WaypointRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class BindModClient implements ClientModInitializer {
	private static BindManager manager;
	private static final AntiAfk ANTI_AFK = new AntiAfk();

	public static BindManager manager() {
		return manager;
	}

	@Override
	public void onInitializeClient() {
		BindModConfig config = BindModConfig.load();
		manager = new BindManager(config);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			manager.onEndClientTick();
			ANTI_AFK.tick(config);
		});

		WaypointRenderer.register(config);

		HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
			ArmorHud.render(graphics, config);
			WaypointHud.render(graphics, config);
		});

		BindMod.LOGGER.info("[Bind Mod] Загружено биндов: {}", config.binds.size());
	}
}
