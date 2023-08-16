package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.CameraInHand;
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
    }

    public abstract boolean isClientSide();

    public Shutter getShutter() {
        return shutter;
    }

    public void activate(Player player, InteractionHand hand) {
        activeCameras.put(player, hand);
        player.startUsingItem(hand);
        player.getLevel().playSound(player, player, Exposure.SoundEvents.VIEWFINDER_OPEN.get(), SoundSource.PLAYERS, 1f,
                player.getLevel().getRandom().nextFloat() * 0.2f + 0.9f);
    }

    public @Nullable InteractionHand deactivate(Player player) {
        player.stopUsingItem();
        @Nullable InteractionHand hand = activeCameras.remove(player);
        if (hand != null) {
            player.getLevel().playSound(player, player, Exposure.SoundEvents.VIEWFINDER_CLOSE.get(), SoundSource.PLAYERS, 1f,
                    player.getLevel().getRandom().nextFloat() * 0.2f + 0.85f);
        }
        return hand;
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

        if (getCameraInHand(player).isEmpty()) {
            if (isActive(player))
                deactivate(player);
        }
    }
}
