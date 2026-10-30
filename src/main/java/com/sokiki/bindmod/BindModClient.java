package com.sokiki.bindmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class BindModClient implements ClientModInitializer {
	private static BindManager manager;

	public static BindManager manager() {
		return manager;
	}

	@Override
	public void onInitializeClient() {
		BindModConfig config = BindModConfig.load();
		manager = new BindManager(config);
		ClientTickEvents.END_CLIENT_TICK.register(client -> manager.onEndClientTick());
		BindMod.LOGGER.info("[Bind Mod] Загружено биндов: {}", config.binds.size());
	}
}
