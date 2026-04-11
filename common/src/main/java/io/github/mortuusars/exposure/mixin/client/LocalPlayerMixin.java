package io.github.mortuusars.exposure.mixin.client;

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
public abstract class LocalPlayerMixin extends Player {
    public LocalPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public void setActiveExposureCamera(Camera camera) {
        super.setActiveExposureCamera(camera);
        CameraClient.setupViewfinder(camera);
    }

    @Override
    public void removeActiveExposureCamera() {
        super.removeActiveExposureCamera();
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
