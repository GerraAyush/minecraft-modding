package com.example;

import java.util.*;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.com.example.HomeCommand;
import main.java.com.example.HomeLocation;

public class HomeMod implements ModInitializer {
	public static final String MOD_ID = "home";
	private static final String KEY = "home_mod_data";
	public static MinecraftServer server;
	private static HomeState state;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            HomeCommand.register(dispatcher);
        });

		// Register server start callback
		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			server = s;
			state = HomeState.get(s.getOverworld());
			LOGGER.info("[HomeMod] Loaded " + state.homeLocations.size() + " players' homes.");
		});
	}


	public static boolean isValidName(String homeName) {
		char[] nameChars = homeName.toCharArray();
		for (char character : nameChars) {
			if (!((character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z') || (character >= '0' && character <= '9'))) {
				return false;
			}
		}
		return true;
	}

	
	public static void addHomeRequest(ServerPlayerEntity sender, String homeName) {
		if (homeName.isEmpty() || !isValidName(homeName.trim())) {
			sender.sendMessage(Text.literal("Please, use a valid name!"), false);
			LOGGER.info("[HomeMod] Player::" + sender.getName().toString() + " didn't use a valid home name.");
			return;
		}

		UUID senderUuid = sender.getUuid();
		ArrayList<HomeLocation> senderHomes = state.homeLocations.get(senderUuid);
		if (senderHomes == null) {
			senderHomes = new ArrayList<HomeLocation>();
		}
		
		if (senderHomes.size() >= 3) {
			sender.sendMessage(Text.literal("Delete a home to add a new one!"), false);
			return;
		}

		String worldId = sender.getWorld().getRegistryKey().getValue().toString();
		double x = sender.getX();
		double y = sender.getY();
		double z = sender.getZ();

		for (int index = 0; index < senderHomes.size(); index++) {
			HomeLocation home = senderHomes.get(index);
			if (home.name().equals(homeName)) {
				sender.sendMessage(Text.literal("Home already exists by this name!"), false);
				return;

			} else if (home.world().equals(worldId) && home.x() == x && home.y() == y && home.z() == z) {
				sender.sendMessage(Text.literal("Home::" + home.name() + " already exists on this location!"), false);
				return;
			}
		}

		senderHomes.add(new HomeLocation(
			homeName,
			worldId,
			x,
			y,
			z,
			sender.getYaw(),
			sender.getPitch()
		));
		state.homeLocations.put(senderUuid, senderHomes);
		HomeMod.state.save();
		sender.sendMessage(Text.literal("Home::" + homeName + " saved successfully!"), false);
	}
	

	public static void delHomeRequest(ServerPlayerEntity sender, String homeName) {
		UUID senderUuid = sender.getUuid();
		ArrayList<HomeLocation> senderHomes = state.homeLocations.get(senderUuid);
		if (senderHomes == null) {
			sender.sendMessage(Text.literal("No home to delete!"), false);
			return;
		}

		int toDel = -1;
		for (int index = 0; index < senderHomes.size(); index++) {
			HomeLocation home = senderHomes.get(index);
			if (home.name().equals(homeName)) {
				toDel = index;
				break;
			}
		}

		if (toDel == -1) {
			sender.sendMessage(Text.literal("Home::" + homeName + " not found!"), false);	
			return;
		}

		senderHomes.remove(toDel);
		HomeMod.state.save();
		sender.sendMessage(Text.literal("Home::" + homeName + " deleted!"), false);
	}


	public static void goHomeRequest(ServerPlayerEntity sender, String homeName) {
		UUID senderUuid = sender.getUuid();
		ArrayList<HomeLocation> senderHomes = state.homeLocations.get(senderUuid);
		if (senderHomes == null) {
			sender.sendMessage(Text.literal("No home to go to!"), false);
			return;
		}

		int toGo = -1;
		for (int index = 0; index < senderHomes.size(); index++) {
			HomeLocation home = senderHomes.get(index);
			if (home.name().equals(homeName)) {
				toGo = index;
				break;
			}
		}

		if (toGo == -1) {
			sender.sendMessage(Text.literal("Home::" + homeName + " not found!"), false);
			return;
		}
		
		HomeLocation home = senderHomes.get(toGo);
		RegistryKey<World> worldKey = RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, Identifier.of(home.world().split(":")[0], home.world().split(":")[1]));
        ServerWorld world = sender.getServer().getWorld(worldKey);

		sender.teleport(
			world,
			home.x(),
			home.y(),
			home.z(),
			Collections.emptySet(),
			home.yaw(),
			home.pitch(),
			true
		);
		sender.sendMessage(Text.literal("Teleported to Home::" + homeName + "!"), false);
	}
}
