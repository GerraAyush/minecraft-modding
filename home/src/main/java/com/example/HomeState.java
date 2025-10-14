package com.example;

import net.minecraft.nbt.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import main.java.com.example.HomeLocation;

import java.util.*;

public class HomeState extends PersistentState {
    public static final String KEY = "home_locations";
    public final Map<UUID, ArrayList<HomeLocation>> homeLocations = new HashMap<>();
    
    public static final Codec<HomeState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(
                Codec.STRING.xmap(UUID::fromString, UUID::toString),
                HomeLocation.CODEC.listOf().xmap(ArrayList::new, list -> list)
            ).fieldOf("homeLocations").forGetter(state -> state.homeLocations)
        ).apply(instance, data -> {
            HomeState state = new HomeState();
            state.homeLocations.putAll(data);
            return state;
        })
    );

    public static final PersistentStateType<HomeState> HOME_STATE_TYPE =
        new PersistentStateType<>(KEY, HomeState::new, CODEC, null); // Pass DataFixTypes.PLAYER if required

    public static HomeState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(HOME_STATE_TYPE);
    }

    private static HomeState fromNbt(NbtCompound tag) {
        HomeState state = new HomeState();

        NbtList players = tag.getList("players").orElse(new NbtList());
        for (NbtElement entry : players) {
            NbtCompound playerTag = (NbtCompound) entry;
            UUID uuid = UUID.fromString(playerTag.getString("uuid").orElse("00000000-0000-0000-0000-000000000000"));
            NbtList homes = playerTag.getList("homes").orElse(new NbtList());
            ArrayList<HomeLocation> homeList = new ArrayList<>();

            for (NbtElement h : homes) {
                NbtCompound home = (NbtCompound) h;
                String name = home.getString("name").orElse("default_name");
                String world = home.getString("world").orElse("default_world");
                double x = home.getDouble("x").orElse(0.0);
                double y = home.getDouble("y").orElse(0.0);
                double z = home.getDouble("z").orElse(0.0);
                float yaw = home.getFloat("yaw").orElse(0f);
                float pitch = home.getFloat("pitch").orElse(0f);
                homeList.add(new HomeLocation(name, world, x, y, z, yaw, pitch));
            }

            state.homeLocations.put(uuid, homeList);
        }

        return state;
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        NbtList players = new NbtList();

        for (Map.Entry<UUID, ArrayList<HomeLocation>> entry : homeLocations.entrySet()) {
            NbtCompound playerTag = new NbtCompound();
            playerTag.putString("uuid", entry.getKey().toString());

            NbtList homes = new NbtList();
            for (HomeLocation home : entry.getValue()) {
                NbtCompound homeTag = new NbtCompound();
                homeTag.putString("name", home.name());
                homeTag.putString("world", home.world());
                homeTag.putDouble("x", home.x());
                homeTag.putDouble("y", home.y());
                homeTag.putDouble("z", home.z());
                homeTag.putFloat("yaw", home.yaw());
                homeTag.putFloat("pitch", home.pitch());
                homes.add(homeTag);
            }

            playerTag.put("homes", homes);
            players.add(playerTag);
        }

        tag.put("players", players);
        return tag;
    }

    public void save() {
        this.markDirty();
    }
}
