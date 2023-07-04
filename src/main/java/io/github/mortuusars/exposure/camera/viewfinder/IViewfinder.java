package io.github.mortuusars.exposure.camera.viewfinder;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public interface IViewfinder {
    void activateAndBroadcast(Player player, InteractionHand hand);

    void deactivateAndBroadcast(Player player);

    boolean isActive(Player player);

    InteractionHand getActiveHand(Player player);
}
