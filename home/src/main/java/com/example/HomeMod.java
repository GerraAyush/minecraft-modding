package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeMod implements ModInitializer {
	public static final String MOD_ID = "home";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            HomeCommand.register(dispatcher, dedicated, environment);
        });

		// Register server start callback
		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			HomeState state = HomeState.get(s.overworld());
			LOGGER.info("[HomeMod] Loaded " + state.getSize() + " players' homes.");
		});
	}
}
