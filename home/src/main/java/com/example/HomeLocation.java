package  com.example;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HomeLocation(String dimension, double x, double y, double z, float yRot, float xRot) {
    public static final Codec<HomeLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("dimension").forGetter(HomeLocation::dimension),
        Codec.DOUBLE.fieldOf("x").forGetter(HomeLocation::x),
        Codec.DOUBLE.fieldOf("y").forGetter(HomeLocation::y),
        Codec.DOUBLE.fieldOf("z").forGetter(HomeLocation::z),
        Codec.FLOAT.fieldOf("yRot").forGetter(HomeLocation::yRot),
        Codec.FLOAT.fieldOf("xRot").forGetter(HomeLocation::xRot)
    ).apply(instance, HomeLocation::new));
}