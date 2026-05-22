package com.example.models;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import com.example.UltimateTpaMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;

public class HomeState extends SavedData {
    private static HomeState instance = null;
    private static final String SAVED_DATA_KEY = "home_mod_data";

    private int maxHomesLimit;
    private final Map<UUID, Map<String, HomeLocation>> homeLocations = new HashMap<>();

    private static final Codec<HomeState> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    // homeLocations Field
                    Codec.unboundedMap(
                            Codec.STRING.xmap(UUID::fromString, UUID::toString),
                            Codec.unboundedMap(Codec.STRING, HomeLocation.CODEC)).fieldOf("homeLocations")
                            .forGetter(s -> s.homeLocations),

                    // maxHomesLimit Field
                    Codec.INT.optionalFieldOf("maxHomesLimit", 5).forGetter(s -> s.maxHomesLimit))

                    .apply(instance, (map, limit) -> {
                        HomeState state = new HomeState();
                        state.homeLocations.putAll(map);
                        state.maxHomesLimit = limit;
                        return state;
                    }));

    private static final SavedDataType<HomeState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(UltimateTpaMod.MOD_ID, SAVED_DATA_KEY),
            HomeState::new,
            CODEC,
            DataFixTypes.LEVEL);

    private HomeState() {
        this.maxHomesLimit = 5;
    }

    public static HomeState get() {
        return instance;
    }

    public static HomeState get(ServerLevel level) {
        HomeState.instance = level.getDataStorage().computeIfAbsent(TYPE);
        return HomeState.instance;
    }

    public int getMaxHomesLimit() {
        return maxHomesLimit;
    }

    public int removeHomeLocation(UUID uuid, String homeName) {
        Map<String, HomeLocation> playerHomes = homeLocations.get(uuid);
        if (playerHomes == null || playerHomes.size() == 0) {
            return -1;
        }

        HomeLocation location = playerHomes.remove(homeName);
        if (location == null) {
            return 0;
        }

        this.save();
        return 1;
    }

    public Map<String, HomeLocation> getHomeLocations(UUID uuid) {
        return homeLocations.get(uuid);
    }

    public void setHomeLocation(UUID uuid, String homeName, HomeLocation location) {
        Map<String, HomeLocation> playerHomes = homeLocations.computeIfAbsent(uuid, k -> new HashMap<>());

        try {
            playerHomes.put(homeName, location);
        } catch (UnsupportedOperationException e) {
            playerHomes = new HashMap<>(playerHomes);
            playerHomes.put(homeName, location);
            homeLocations.put(uuid, playerHomes);
        }
        this.save();
    }

    public int getSize() {
        return homeLocations.size();
    }

    public void save() {
        this.setDirty();
    }
}
