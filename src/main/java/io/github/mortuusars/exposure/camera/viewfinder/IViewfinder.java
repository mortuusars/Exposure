package io.github.mortuusars.exposure.camera.viewfinder;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public interface IViewfinder {
    void activate(Player player);
    void deactivate(Player player);
    boolean isActive(Player player);
    void update();
}
