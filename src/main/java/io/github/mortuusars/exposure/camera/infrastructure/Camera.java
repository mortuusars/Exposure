package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Camera {
    private final Map<Player, InteractionHand> activeCameras = new HashMap<>();

    private final Shutter shutter;

    public Camera(Shutter shutter) {
        this.shutter = shutter;
    }

    public abstract boolean isClientSide();

    public Shutter getShutter() {
        return shutter;
    }

    public void activate(Player player, InteractionHand hand) {
        activeCameras.put(player, hand);
    }

    public @Nullable InteractionHand deactivate(Player player) {
        return activeCameras.remove(player);
    }

    public void clear() {
        activeCameras.clear();
        shutter.clear();
    }

    public Optional<InteractionHand> getActiveHand(Player player) {
        return Optional.ofNullable(activeCameras.get(player));
    }

    public boolean isActive(Player player) {
        return getActiveHand(player).isPresent();
    }

    public CameraInHand getCameraInHand(Player player) {
        return new CameraInHand(player);
    }

    public void tick(Player player) {
        shutter.tick(player);

        if (isActive(player) && getCameraInHand(player).isEmpty()) {
            deactivate(player);
            return;
        }
    }
}
