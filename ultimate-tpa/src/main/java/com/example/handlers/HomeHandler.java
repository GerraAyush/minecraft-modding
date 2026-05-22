package com.example.handlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.UltimateTpaMod;
import com.example.models.HomeLocation;
import com.example.models.HomeState;
import com.example.models.ServerLocation;
import com.example.models.ServerLocationManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class HomeHandler {

    private HomeState homeState;
    private static Logger LOGGER = LoggerFactory.getLogger(UltimateTpaMod.MOD_ID);

    private boolean isValidName(String homeName) {
        char[] nameChars = homeName.toCharArray();
        for (char character : nameChars) {
            if (!((character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z')
                    || (character >= '0' && character <= '9'))) {
                return false;
            }
        }
        return true;
    }

    private void addHome(ServerPlayer player, String homeName) {
        LOGGER.info("[HomeMod] Player::%s called addHome.".formatted(player.getName().getString()));
        if (homeState == null) {
            homeState = HomeState.get();
        }

        if (homeName.isEmpty() || !isValidName(homeName.trim())) {
            player.sendSystemMessage(Component.literal("Please, use a valid name!"));
            LOGGER.info("[HomeMod] Player::%s didn't use a valid home name.".formatted(player.getName().getString()));
            return;
        }

        UUID playerUuid = player.getUUID();
        Map<String, HomeLocation> playerHomes = homeState.getHomeLocations(playerUuid);
        if (playerHomes == null) {
            playerHomes = new HashMap<String, HomeLocation>();
        }

        if (playerHomes.size() >= homeState.getMaxHomesLimit()) {
            player.sendSystemMessage(Component.literal("Delete a home to add a new one!"));
            return;
        }

        HomeLocation home = playerHomes.get(homeName);
        if (home != null) {
            player.sendSystemMessage(Component.literal("%s already used!".formatted(homeName)));
            return;
        }

        String dimensionStringId = player.level().dimension().toString().split(" / ")[1].replaceAll("]", "");
        double xCoord = player.getX();
        double yCoord = player.getY();
        double zCoord = player.getZ();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        for (String key : playerHomes.keySet()) {
            HomeLocation location = playerHomes.get(key);

            if (location.dimension().equals(dimensionStringId) && location.x() == xCoord && location.y() == yCoord
                    && location.z() == zCoord) {
                player.sendSystemMessage(
                        Component.literal("Home::%s already exists on this location!".formatted(homeName)));
                return;
            }
        }

        homeState.setHomeLocation(
                playerUuid,
                homeName,
                new HomeLocation(dimensionStringId, xCoord, yCoord, zCoord, yaw, pitch));
        player.sendSystemMessage(Component.literal("Home::%s saved successfully!".formatted(homeName)));
    }

    private void deleteHome(ServerPlayer player, String homeName) {
        LOGGER.info("[HomeMod] Player::%s called deleteHome.".formatted(player.getName().getString()));

        if (homeState == null) {
            homeState = HomeState.get();
        }

        UUID playerUuid = player.getUUID();
        int result = homeState.removeHomeLocation(playerUuid, homeName);
        switch (result) {
            case -1:
                player.sendSystemMessage(Component.literal("No home to delete!"));
                break;

            case 0:
                player.sendSystemMessage(Component.literal("Home::%s not found!".formatted(homeName)));
                break;

            case 1:
                player.sendSystemMessage(Component.literal("Home::%s deleted!".formatted(homeName)));
                break;
        }
    }

    private void gotoHome(ServerPlayer player, String homeName) {
        LOGGER.info("[HomeMod] Player::%s called gotoHome.".formatted(player.getName().getString()));
        if (homeState == null) {
            homeState = HomeState.get();
        }
        UUID playerUuid = player.getUUID();
        Map<String, HomeLocation> playerHomes = homeState.getHomeLocations(playerUuid);
        if (playerHomes == null || playerHomes.size() == 0) {
            player.sendSystemMessage(Component.literal("No home to go to!"));
            return;
        }

        HomeLocation location = playerHomes.get(homeName);
        if (location == null) {
            player.sendSystemMessage(Component.literal("Home::%s not found!".formatted(homeName)));
            return;
        }

        String[] parts = location.dimension().split(":", 2);
        Identifier dimensionIdentifier = Identifier.fromNamespaceAndPath(parts[0], parts[1]);
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionIdentifier);

        MinecraftServer server = player.level().getServer();
        ServerLevel targetLevel = server.getLevel(dimensionKey);
        ServerLocationManager.putPreviousLocation(
                playerUuid,
                new ServerLocation(targetLevel, player.getX(), player.getY(), player.getZ(),
                        player.getYRot(), player.getXRot()));

        player.teleportTo(targetLevel, location.x(), location.y(), location.z(),
                Collections.emptySet(), location.yRot(), location.xRot(), false);
        player.sendSystemMessage(Component.literal("Teleported to Home::%s!".formatted(homeName)));
    }

    private void listHomes(ServerPlayer player) {
        LOGGER.info("[HomeMod] Player::%s called listHomes.".formatted(player.getName().getString()));

        if (homeState == null) {
            homeState = HomeState.get();
        }

        UUID playerUuid = player.getUUID();
        Map<String, HomeLocation> playerHomes = homeState.getHomeLocations(playerUuid);
        if (playerHomes == null || playerHomes.size() == 0) {
            player.sendSystemMessage(Component.literal("No home to list!"));
            return;
        }

        StringBuilder stringBuilder = new StringBuilder("List of homes:\n");

        int index = 0;
        for (String homeName : playerHomes.keySet()) {
            stringBuilder.append("%d. %s\n".formatted((index + 1), homeName));
            index++;
        }

        player.sendSystemMessage(Component.literal(stringBuilder.toString()), false);
    }

    public int dispatcher(String command, CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        if (command.equals("list")) {
            listHomes(player);
        }

        else {
            String target = StringArgumentType.getString(context, "target");
            switch (command) {
                case "go":
                    gotoHome(player, target);
                    break;
                case "add":
                    addHome(player, target);
                    break;
                case "delete":
                    deleteHome(player, target);
                    break;
                default:
                    return -1;
            }
        }

        return 1;
    }
}
