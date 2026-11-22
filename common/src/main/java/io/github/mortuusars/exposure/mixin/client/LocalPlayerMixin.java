package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.world.entity.CameraOperator;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player implements CameraOperator {
    public LocalPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public void onExposureCameraSet(Camera camera) {
        CameraClient.setupViewfinder(camera);
    }

    @Override
    public void onExposureCameraRemoved() {
        CameraClient.removeViewfinder();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        CameraClient.tick();

        getActiveExposureCameraOptional().ifPresent(camera -> {
            if (!camera.isActive()) {
                removeActiveExposureCamera();
            }
        });
    }
}
