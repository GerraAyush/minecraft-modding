package  main.java.com.example.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.World;


public record HomeLocation(String name, String world, double x, double y, double z, float yaw, float pitch) {
    public static final Codec<HomeLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(HomeLocation::name),
        Codec.STRING.fieldOf("world").forGetter(HomeLocation::world),
        Codec.DOUBLE.fieldOf("x").forGetter(HomeLocation::x),
        Codec.DOUBLE.fieldOf("y").forGetter(HomeLocation::y),
        Codec.DOUBLE.fieldOf("z").forGetter(HomeLocation::z),
        Codec.FLOAT.fieldOf("yaw").forGetter(HomeLocation::yaw),
        Codec.FLOAT.fieldOf("pitch").forGetter(HomeLocation::pitch)
    ).apply(instance, HomeLocation::new));
}