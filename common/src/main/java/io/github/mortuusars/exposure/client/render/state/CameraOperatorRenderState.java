package io.github.mortuusars.exposure.client.render.state;

import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import org.jetbrains.annotations.Nullable;

public interface CameraOperatorRenderState {
    default @Nullable Camera getExposureCamera() {
        throw new IllegalStateException("This method must be implemented.");
    }

    default void setExposureCamera(Camera camera) {
        throw new IllegalStateException("This method must be implemented.");
    }

    default boolean isCurrentPlayer() {
        throw new IllegalStateException("This method must be implemented.");
    }

    default void setIsCurrentPlayer(boolean isCurrentPlayer) {
        throw new IllegalStateException("This method must be implemented.");
    }

    default boolean isCameraEntity() {
        throw new IllegalStateException("This method must be implemented.");
    }

    default void setIsCameraEntity(boolean isCameraEntity) {
        throw new IllegalStateException("This method must be implemented.");
    }

    default float getExposureCameraActionAnim() {
        return 0F;
    }

    default void setExposureCameraActionAnim(float cameraActionAnim) {
        throw new IllegalStateException("This method must be implemented.");
    }

    default CameraInHand getFoundCameraInHand() {
        throw new IllegalStateException("This method must be implemented.");
    }

    default void setFoundCameraInHand(CameraInHand cameraInHand) {
        throw new IllegalStateException("This method must be implemented.");
    }
}
