package com.example.mixin;

import com.example.TpaMod;
import main.java.com.example.ServerLocation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayerEntity.class)
public class PlayerDeathMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource source, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayerEntity player)) return;

        World world = player.getWorld();
        TpaMod.previousLocations.put(player.getUuid(), new ServerLocation(
            player.getServerWorld(),
            player.getX(),
            player.getY(),
            player.getZ(),
            player.getYaw(),
            player.getPitch()
        ));
    }
}
