package main.java.com.example.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.UltimateTpaMod;
import main.java.com.example.models.ServerLocation;


@Mixin(ServerPlayerEntity.class)
public class PlayerDeathMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource source, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayerEntity player)) return;

        World world = player.getWorld();
        UltimateTpaMod.previousLocations.put(player.getUuid(), new ServerLocation(
            player.getServerWorld(),
            player.getX(),
            player.getY(),
            player.getZ(),
            player.getYaw(),
            player.getPitch()
        ));
    }
}
