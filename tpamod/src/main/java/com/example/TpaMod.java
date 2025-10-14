package com.example;

import net.fabricmc.api.ModInitializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import net.minecraft.server.world.ServerWorld; 
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;

import java.util.UUID;

import javax.swing.text.html.parser.Entity;

import main.java.com.example.TpaCommand;
import main.java.com.example.ServerLocation;

public class TpaMod implements ModInitializer {
    public static final String MOD_ID = "tpamod";

    private static final Map<UUID, UUID> pendingRequests = new HashMap<>();
	public static final Map<UUID, ServerLocation> previousLocations = new HashMap<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            TpaCommand.register(dispatcher);
        });
    }

    public static void sendTpaRequest(ServerPlayerEntity sender, ServerPlayerEntity target) {
		if (target.getUuid() != sender.getUuid()) {
			pendingRequests.put(target.getUuid(), sender.getUuid());
			target.sendMessage(Text.literal(sender.getName().getString() + " has requested to teleport to you. Type /tpaccept to allow."), false);
			sender.sendMessage(Text.literal("TPA request sent to " + target.getName().getString()), false);
			return;
		}
		sender.sendMessage(Text.literal("Nice Joke!"), false);
    }

    public static void acceptTpaRequest(ServerPlayerEntity accepter) {
        UUID senderUuid = pendingRequests.remove(accepter.getUuid());
        if (senderUuid == null) {
            accepter.sendMessage(Text.literal("No pending TPA requests."), false);
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
            sender.sendMessage(Text.literal("Teleported to " + accepter.getName().getString()), false);
            accepter.sendMessage(Text.literal("Accepted teleport request from " + sender.getName().getString()), false);
        } else {
            accepter.sendMessage(Text.literal("Player not found."), false);
        }
    }
	
	public static void denyTpaRequest(ServerPlayerEntity denier) {
		UUID senderUuid = pendingRequests.remove(denier.getUuid());
		if (senderUuid == null) {
			denier.sendMessage(Text.literal("No pending TPA requests."), false);
            return;
		}

		ServerPlayerEntity sender = denier.getServer().getPlayerManager().getPlayer(senderUuid);
		if (sender != null) {
			sender.sendMessage(Text.literal("Request denied by " + denier.getName().getString()), false);
			denier.sendMessage(Text.literal("Denied teleport request from " + sender.getName().getString()), false);
		} else {
			denier.sendMessage(Text.literal("Player not found."), false);
		}
	}

	public static void back(ServerPlayerEntity player) {
		ServerLocation lastLoc = previousLocations.get(player.getUuid());
		if (lastLoc == null) {
			player.sendMessage(Text.literal("No location available."), false);
            return;
		}
            
		player.teleport(lastLoc.world(), lastLoc.x(), lastLoc.y(), lastLoc.z(), Collections.emptySet(), lastLoc.yaw(), lastLoc.pitch(), true);
		player.sendMessage(Text.literal("Teleported back to your previous location."), false);
	}
}
