package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.component.Shutter;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        this.shutter.setOnShutterClosed(this::onShutterClosed);
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

    public Optional<InteractionHand> getActiveHand(Player player) {
        return Optional.ofNullable(activeCameras.get(player));
    }

    public boolean isActive(Player player) {
        return getActiveHand(player).isPresent();
    }

    public CameraInHand getCameraInHand(Player player) {
        return new CameraInHand(player);
    }

    public void onShutterClosed(Player player, Shutter.OpenShutter shutter) {
        CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
        if (!camera.isEmpty()) {
            camera.getItem().getAttachments(camera.getStack()).getFilm().ifPresent(film -> {
                if (film.getItem().canAddFrame(film.getStack()))
                    player.getLevel().playSound(player, player, SoundEvents.UI_LOOM_SELECT_PATTERN, SoundSource.PLAYERS, 0.9f, 1f);
            });
        }
    }

    public void tick(Player player) {
        shutter.tick(player);

        if (getCameraInHand(player).isEmpty()) {
            deactivate(player);
            return;
        }
    }
}
