package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.render.state.CameraOperatorRenderState;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public abstract class PlayerRenderStateMixin implements CameraOperatorRenderState {
    @Unique
    @Nullable
    public Camera exposure$Camera;
    @Unique
    @Nullable
    public CameraInHand exposure$CameraInHand;
    @Unique
    public boolean exposure$IsCurrentPlayer;
    @Unique
    public boolean exposure$IsCameraEntity;
    @Unique
    public float exposure$CameraActionAnim = 0F;


    @Override
    public @Nullable Camera getExposureCamera() {
        return exposure$Camera;
    }

    @Override
    public void setExposureCamera(Camera camera) {
        exposure$Camera = camera;
    }

    @Override
    public boolean isCurrentPlayer() {
        return exposure$IsCurrentPlayer;
    }

    @Override
    public void setIsCurrentPlayer(boolean isCurrentPlayer) {
        exposure$IsCurrentPlayer = isCurrentPlayer;
    }

    public boolean isCameraEntity() {
        return exposure$IsCameraEntity;
    }

    public void setIsCameraEntity(boolean isCameraEntity) {
        exposure$IsCameraEntity = isCameraEntity;
    }

    @Override
    public float getExposureCameraActionAnim() {
        return exposure$CameraActionAnim;
    }

    @Override
    public void setExposureCameraActionAnim(float cameraActionAnim) {
        exposure$CameraActionAnim = cameraActionAnim;
    }

    @Override
    public CameraInHand getFoundCameraInHand() {
        return exposure$CameraInHand;
    }

    @Override
    public void setFoundCameraInHand(CameraInHand cameraInHand) {
        exposure$CameraInHand = cameraInHand;
    }
}
