package main.java.com.example.models;

import net.minecraft.server.world.ServerWorld; 


public record ServerLocation(ServerWorld world, double x, double y, double z, float yaw, float pitch) {}
