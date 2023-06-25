package io.github.mortuusars.exposure.camera.viewfinder;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public interface IViewfinderNew {
    void activate(Player player, InteractionHand hand);

    void deactivate(Player player);

    boolean isActive(Player player);

    InteractionHand getActiveHand(Player player);
}
