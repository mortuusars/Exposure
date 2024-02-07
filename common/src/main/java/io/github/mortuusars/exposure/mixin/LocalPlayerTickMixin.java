package io.github.mortuusars.exposure.mixin;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerTickMixin extends Player {
    public LocalPlayerTickMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void onPlayerTickEnd(CallbackInfo ci) {
        ViewfinderClient.onPlayerTick(this);
    }
}
