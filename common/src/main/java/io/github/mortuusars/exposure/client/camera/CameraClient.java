package io.github.mortuusars.exposure.client.camera;

import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderRegistry;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.common.ActiveCameraDeactivateCommonPacket;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CameraClient {
    private static @Nullable Viewfinder activeViewfinder;

    public static void tick() {
        if (activeViewfinder != null) {
            activeViewfinder.tick();
        }
    }

    public static Optional<Camera> getActive() {
        return Minecrft.playerOrThrow().getActiveExposureCameraOptional();
    }

    public static boolean isActive() {
        return getActive().isPresent();
    }

    public static void deactivate() {
        Minecrft.playerOrThrow().getActiveExposureCameraOptional().ifPresent(
              camera -> {
                  camera.map((item, stack) -> item.deactivate(Minecrft.playerOrThrow(), camera.getHolder().asHolderEntity(), stack));
                  Minecrft.playerOrThrow().removeActiveExposureCamera();
                  // Fixes camera getting stuck in some cases, requiring a relog to fix
                  if (Minecrft.get().getCameraEntity() instanceof CameraStandEntity) {
                      resetCameraEntity();
                  }
              });
        Packets.sendToServer(ActiveCameraDeactivateCommonPacket.INSTANCE);
    }

    public static void setCameraEntity(@Nullable Entity entity) {
        // Not using Minecraft#setCameraEntity because it updates postEffect
        Minecrft.get().cameraEntity = entity;

        if (entity != null) {
            // Set eye height to final value to skip transition animation
            Minecrft.get().gameRenderer.getMainCamera().eyeHeight = entity.getEyeHeight();
            Minecrft.get().gameRenderer.getMainCamera().eyeHeightOld = entity.getEyeHeight();
            Minecrft.get().gameRenderer.getMainCamera().reset();
        }
    }

    public static void resetCameraEntity() {
        setCameraEntity(Minecrft.playerOrThrow());
    }

    // --

    public static @Nullable Viewfinder viewfinder() {
        return activeViewfinder;
    }

    public static void setupViewfinder(@NotNull Camera camera) {
        removeViewfinder();
        activeViewfinder = ViewfinderRegistry.get(camera);
        activeViewfinder.setup();
    }

    public static void removeViewfinder() {
        if (activeViewfinder != null) {
            activeViewfinder.close();
            activeViewfinder = null;
        }
    }
}