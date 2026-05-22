package com.example;

import net.minecraft.server.level.ServerLevel;

public record ServerLocation(ServerLevel level, double x, double y, double z, float yaw, float pitch) {}
