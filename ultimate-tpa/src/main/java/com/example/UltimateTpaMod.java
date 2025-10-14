package com.example;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.swing.text.html.parser.Entity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource; // Note: ServerCommandSource extends CommandSource
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import main.java.com.example.commands.TpaCommand;
import main.java.com.example.commands.HomeCommand;
import main.java.com.example.models.HomeState;
import main.java.com.example.models.HomeLocation;
import main.java.com.example.models.ServerLocation;


public class UltimateTpaMod implements ModInitializer {
	public static final String MOD_ID = "utpa";

	private static final String KEY = "home_mod_data";
	private static HomeState state;
	public static MinecraftServer server;
    private static final Map<UUID, UUID> pendingRequests = new HashMap<>();
	public static final Map<UUID, ServerLocation> previousLocations = new HashMap<>();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            TpaCommand.register(dispatcher);
        });
		
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            HomeCommand.register(dispatcher);
        });

		// Register server start callback
		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			server = s;
			state = HomeState.get(s.getOverworld());
			LOGGER.info("[UltimateTpaMod] Loaded " + state.homeLocations.size() + " players' homes.");
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
		LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Add Home Request");
		
		if (homeName.isEmpty() || !isValidName(homeName.trim())) {
			sender.sendMessage(Text.literal("[UltimateTpaMod] Add Home Failed (Invalid Name)"), false);
			LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Add Home Failed (Invalid Name)");
			return;
		}
		
		UUID senderUuid = sender.getUuid();
		ArrayList<HomeLocation> senderHomes = state.homeLocations.get(senderUuid);
		if (senderHomes == null) {
			senderHomes = new ArrayList<HomeLocation>();
		}
		
		if (senderHomes.size() >= 3) {
			sender.sendMessage(Text.literal("[UltimateTpaMod] Home Limit Reached!"), false);
			LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Home Limit Reached");
			return;
		}
		
		String worldId = sender.getWorld().getRegistryKey().getValue().toString();
		double x = sender.getX();
		double y = sender.getY();
		double z = sender.getZ();
		
		for (int index = 0; index < senderHomes.size(); index++) {
			HomeLocation home = senderHomes.get(index);
			if (home.name().equals(homeName)) {
				sender.sendMessage(Text.literal("[UltimateTpaMod] Name already used"), false);
				LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Name already used");
				return;
				
			} else if (home.world().equals(worldId) && home.x() == x && home.y() == y && home.z() == z) {
				sender.sendMessage(Text.literal("[UltimateTpaMod] Home:" + home.name() + " exists on this location!"), false);
				LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Has a house on the specified location");
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
			UltimateTpaMod.state.save();
			sender.sendMessage(Text.literal("[UltimateTpaMod] Saved Successfully (" + homeName + ")!"), false);
			LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Saved Successfully(" + homeName + ")!");
		}
		
		
	public static void delHomeRequest(ServerPlayerEntity sender, String homeName) {
		LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Delete Home Request");
		
		UUID senderUuid = sender.getUuid();
		ArrayList<HomeLocation> senderHomes = state.homeLocations.get(senderUuid);
		if (senderHomes == null) {
			sender.sendMessage(Text.literal("[UltimateTpaMod] No home to delete!"), false);
			LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => No home to delete!");
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
			sender.sendMessage(Text.literal("[UltimateTpaMod] Not Found (" + homeName + ")!"), false);	
			LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Not Found(" + homeName + ")!");
			return;
		}
		
		senderHomes.remove(toDel);
		UltimateTpaMod.state.save();
		sender.sendMessage(Text.literal("[UltimateTpaMod] Deleted(" + homeName + ")!"), false);
		LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Deleted (" + homeName + ")!");
	}
	
	
	public static void goHomeRequest(ServerPlayerEntity sender, String homeName) {
		LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Go Home Request");
		
		UUID senderUuid = sender.getUuid();
		ArrayList<HomeLocation> senderHomes = state.homeLocations.get(senderUuid);
		if (senderHomes == null) {
			sender.sendMessage(Text.literal("[UltimateTpaMod] No home to go to!"), false);
			LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => No home to go to!");
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
			sender.sendMessage(Text.literal("[UltimateTpaMod] Not Found (" + homeName + ")!"), false);
			LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Not Found(" + homeName + ")!");
			return;
		}
		
		HomeLocation home = senderHomes.get(toGo);
		RegistryKey<World> worldKey = RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, Identifier.of(home.world().split(":")[0], home.world().split(":")[1]));
        ServerWorld world = sender.getServer().getWorld(worldKey);
		
		previousLocations.put(sender.getUuid(),
		new ServerLocation(
			world,
			sender.getX(),
			sender.getY(),
			sender.getZ(),
			sender.getYaw(),
				sender.getPitch()
				)
				);
				
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
			sender.sendMessage(Text.literal("[UltimateTpaMod] Teleported To (" + homeName + ")!"), false);
			LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Teleported To (" + homeName + ")!");
		}
		
		
		public static void sendTpaRequest(ServerPlayerEntity sender, ServerPlayerEntity target) {
			LOGGER.info("[UltimateTpaMod] "  + sender.getName().toString() + " => Send TPA Request");

			if (target.getUuid() != sender.getUuid()) {
				pendingRequests.put(target.getUuid(), sender.getUuid());
				target.sendMessage(Text.literal("[UltimateTpaMod] " + sender.getName().getString() + " has requested to teleport to you. Type /tpaccept to allow."), false);
				sender.sendMessage(Text.literal("[UltimateTpaMod] TPA request sent to " + target.getName().getString()), false);
				LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => TPA Request => " + target.getName().getString());
				return;
			}
			sender.sendMessage(Text.literal("[UltimateTpaMod] Nice Joke!"), false);
			LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => TPA Request == Nice Joke!");
		}
		

    public static void acceptTpaRequest(ServerPlayerEntity accepter) {
		LOGGER.info("[UltimateTpaMod] "  + accepter.getName().toString() + " => Accept TPA Request");

        UUID senderUuid = pendingRequests.remove(accepter.getUuid());
        if (senderUuid == null) {
            accepter.sendMessage(Text.literal("[UltimateTpaMod] No pending TPA requests."), false);
			LOGGER.info("[UltimateTpaMod] " + accepter.getName().toString() + " => No pending TPA requests.");
            return;
        }

        ServerPlayerEntity sender = accepter.getServer().getPlayerManager().getPlayer(senderUuid);
        if (sender != null) {
			ServerWorld targetWorld = (ServerWorld) accepter.getWorld();

			previousLocations.put(
				sender.getUuid(), 
				new ServerLocation(targetWorld, sender.getX(), sender.getY(), sender.getZ(), sender.getYaw(), sender.getPitch())
			);

			sender.teleport(
				targetWorld, // The world to teleport to
				accepter.getX(), // Target X coordinate
				accepter.getY(), // Target Y coordinate
				accepter.getZ(), // Target Z coordinate
				Collections.emptySet(), // Flags for position. Use an empty set for basic teleport.
				accepter.getYaw(), // Target Yaw (horizontal rotation)
				accepter.getPitch(), // Target Pitch (vertical rotation)
				true // dismount: true to dismount from any entity they are riding
			);
            sender.sendMessage(Text.literal("[UltimateTpaMod] Teleported to " + accepter.getName().getString()), false);
            accepter.sendMessage(Text.literal("[UltimateTpaMod] Accepted teleport request from " + sender.getName().getString()), false);
			LOGGER.info("[UltimateTpaMod] " + sender.getName().toString() + " => Teleported to " + accepter.getName().getString());
        } else {
			accepter.sendMessage(Text.literal("[UltimateTpaMod] Unknown player!"), false);
			LOGGER.info("[UltimateTpaMod] " + accepter.getName().toString() + " tried to accept TPA request from Unknown player!");
        }
    }
	

	public static void denyTpaRequest(ServerPlayerEntity denier) {
		LOGGER.info("[UltimateTpaMod] " + denier.getName().toString() + " => Deny TPA Request");
		
		UUID senderUuid = pendingRequests.remove(denier.getUuid());
		if (senderUuid == null) {
			denier.sendMessage(Text.literal("[UltimateTpaMod] No pending TPA requests."), false);
			LOGGER.info("[UltimateTpaMod] " + denier.getName().toString() + " => No pending TPA requests.");
            return;
		}

		ServerPlayerEntity sender = denier.getServer().getPlayerManager().getPlayer(senderUuid);
		if (sender != null) {
			sender.sendMessage(Text.literal("[UltimateTpaMod] Request denied by " + denier.getName().getString()), false);
			denier.sendMessage(Text.literal("[UltimateTpaMod] Denied teleport request from " + sender.getName().getString()), false);
			LOGGER.info("[UltimateTpaMod] " + denier.getName().toString() + " => Denied request from " + sender.getName().getString());
		} else {
			denier.sendMessage(Text.literal("[UltimateTpaMod] Unknown player!"), false);
			LOGGER.info("[UltimateTpaMod] " + denier.getName().toString() + " tried to deny TPA request from Unknown player!");
		}
	}


	public static void back(ServerPlayerEntity player) {
		LOGGER.info("[UltimateTpaMod] " + player.getName().toString() + " => Back Request");

		ServerLocation lastLoc = previousLocations.get(player.getUuid());
		if (lastLoc == null) {
			player.sendMessage(Text.literal("[UltimateTpaMod] No location available."), false);
            return;
		}
            
		player.teleport(lastLoc.world(), lastLoc.x(), lastLoc.y(), lastLoc.z(), Collections.emptySet(), lastLoc.yaw(), lastLoc.pitch(), true);
		player.sendMessage(Text.literal("[UltimateTpaMod] Teleported back to your previous location."), false);
	}
}
