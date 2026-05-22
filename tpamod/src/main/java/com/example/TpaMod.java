package com.example;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TpaMod implements ModInitializer {
    public static final String MOD_ID = "tpamod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
		LOGGER.info("Initializing TPA Mod");

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            TpaCommand.register(dispatcher);
        });
    }
}
