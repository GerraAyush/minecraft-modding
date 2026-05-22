package com.example.handlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import com.example.models.ServerLocation;
import com.example.models.ServerLocationManager;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class TpaHandler {

    private static final Map<UUID, Stack<UUID>> pendingRequests = new HashMap<>();

    public static void requestTeleport(ServerPlayer requestor, ServerPlayer target) {
        UUID targetUuid = target.getUUID();
        UUID requestorUuid = requestor.getUUID();

        if (targetUuid == requestorUuid) {
            requestor.sendSystemMessage(Component.literal("Nice Joke!"), false);
            return;
        }

        Stack<UUID> existingRequests = pendingRequests.get(targetUuid);

        if (existingRequests == null) {
            existingRequests = new Stack<>();
            pendingRequests.put(targetUuid, existingRequests);
        }

        existingRequests.push(requestorUuid);
        target.sendSystemMessage(Component.literal(
                requestor.getName().getString() + " has requested to teleport to you. Type /tpaccept to allow."));
        requestor.sendSystemMessage(Component.literal("TPA request sent to " + target.getName().getString()));
        return;
    }

    public static void acceptTeleport(CommandSourceStack context) {
        MinecraftServer server = context.getServer();
        ServerPlayer player = context.getPlayer();

        UUID playerUuid = player.getUUID();
        Stack<UUID> playerRequests = pendingRequests.get(playerUuid);
        if (playerRequests == null) {
            pendingRequests.put(playerUuid, new Stack<>());
            player.sendSystemMessage(Component.literal("No pending TPA requests."));
            return;
        }

        if (playerRequests.size() == 0) {
            player.sendSystemMessage(Component.literal("No pending TPA requests."));
            return;
        }

        UUID requestorUuid = playerRequests.pop();
        PlayerList serverPlayers = server.getPlayerList();
        ServerPlayer requestor = serverPlayers.getPlayer(requestorUuid);
        if (requestor == null) {
            player.sendSystemMessage(Component.literal("Player no longer available."));
            return;
        }

        ServerLevel targetLevel = context.getLevel();
        ServerLocationManager.putPreviousLocation(
                requestorUuid,
                new ServerLocation(targetLevel, requestor.getX(), requestor.getY(), requestor.getZ(),
                        requestor.getYRot(), requestor.getXRot()));

        requestor.teleportTo(targetLevel, player.getX(), player.getY(), player.getZ(), Collections.emptySet(),
                player.getYRot(), player.getXRot(), false);
        requestor.sendSystemMessage(Component.literal("Teleported to " + player.getName().getString()));
        player.sendSystemMessage(Component.literal("Accepted teleport request from " + player.getName().getString()));
    }

    public static void denyTpaRequest(CommandSourceStack context) {
        MinecraftServer server = context.getServer();
        ServerPlayer player = context.getPlayer();

        UUID playerUuid = player.getUUID();
        Stack<UUID> playerRequests = pendingRequests.get(playerUuid);
        if (playerRequests == null) {
            pendingRequests.put(playerUuid, new Stack<>());
            player.sendSystemMessage(Component.literal("No pending TPA requests."));
            return;
        }

        if (playerRequests.size() == 0) {
            player.sendSystemMessage(Component.literal("No pending TPA requests."));
            return;
        }

        UUID requestorUuid = playerRequests.pop();
        PlayerList serverPlayers = server.getPlayerList();
        ServerPlayer requestor = serverPlayers.getPlayer(requestorUuid);
        if (requestor == null) {
            player.sendSystemMessage(Component.literal("Player no longer available."));
            return;
        }

        requestor.sendSystemMessage(Component.literal("Request denied by " + player.getName().getString()));
        player.sendSystemMessage(Component.literal("Denied teleport request from " + requestor.getName().getString()));
    }

    public static void teleportBack(CommandSourceStack context) {
        ServerPlayer player = context.getPlayer();

        ServerLocation previousLocation = ServerLocationManager.getPreviousLocation(player.getUUID(),
                new ServerLocation(player.level(), player.getX(), player.getY(), player.getZ(),
                        player.getYRot(), player.getXRot()));
        if (previousLocation == null) {
            player.sendSystemMessage(Component.literal("No location available."));
            return;
        }

        player.teleportTo(previousLocation.level(), previousLocation.x(), previousLocation.y(),
                previousLocation.z(), Collections.emptySet(), previousLocation.yaw(), previousLocation.pitch(), false);
        player.sendSystemMessage(Component.literal("Teleported back to your previous location."));
    }

    public static void teleportFront(CommandSourceStack context) {
        ServerPlayer player = context.getPlayer();

        ServerLocation frontLocation = ServerLocationManager.getFrontLocation(player.getUUID(),
                new ServerLocation(player.level(), player.getX(), player.getY(), player.getZ(),
                        player.getYRot(), player.getXRot()));
        if (frontLocation == null) {
            player.sendSystemMessage(Component.literal("No location available."));
            return;
        }

        player.teleportTo(frontLocation.level(), frontLocation.x(), frontLocation.y(),
                frontLocation.z(), Collections.emptySet(), frontLocation.yaw(), frontLocation.pitch(), false);
        player.sendSystemMessage(Component.literal("Teleported back to your front location."));
    }
}
