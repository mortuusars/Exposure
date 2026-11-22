package io.github.mortuusars.exposure.world.entity;

import io.github.mortuusars.exposure.world.camera.Camera;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Injected in Entities such as {@link net.minecraft.world.entity.player.Player}. <br>
 * Injected interfaces must have all methods as 'default'.
 */
public interface CameraOperator {
    default @Nullable Camera getActiveExposureCamera() {
        throw new IllegalStateException("This method must be implemented.");
    }

    default void setActiveExposureCamera(Camera camera) {
        throw new IllegalStateException("This method must be implemented.");
    }

    default void removeActiveExposureCamera() {
        throw new IllegalStateException("This method must be implemented.");
    }

    default void onExposureCameraSet(Camera camera) {}

    default void onExposureCameraRemoved() {}

    // --

    default Optional<Camera> getActiveExposureCameraOptional() {
        return Optional.ofNullable(getActiveExposureCamera());
    }

    default float getExposureCameraActionAnim(float partialTick) {
        return 0F;
    }

    default LivingEntity asOperatorEntity() {
        return (LivingEntity) this;
    }
}
