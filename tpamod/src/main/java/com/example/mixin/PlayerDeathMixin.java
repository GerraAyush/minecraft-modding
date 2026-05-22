package com.example.mixin;

import com.example.ServerLocation;
import com.example.ServerLocationManager;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class PlayerDeathMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource source, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player))
            return;

        ServerLevel targetLevel = player.level();
        ServerLocationManager.putPreviousLocation(
                player.getUUID(),
                new ServerLocation(targetLevel, player.getX(), player.getY(), player.getZ(),
                        player.getYRot(), player.getXRot()));
    }
}
