package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import com.example.commands.HomeCommand;
import com.example.commands.TpaCommand;
import com.example.models.HomeState;


public class UltimateTpaMod implements ModInitializer {
	public static final String MOD_ID = "utpa";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
		LOGGER.info("Initializing Ultimate TPA Mod");

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            TpaCommand.register(dispatcher, dedicated, environment);
        });

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
