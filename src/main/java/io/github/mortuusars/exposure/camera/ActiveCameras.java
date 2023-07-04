package io.github.mortuusars.exposure.camera;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class ActiveCameras {
    private final Map<Player, InteractionHand> activeCameras = new HashMap<>();

    public void activate(Player player, InteractionHand hand) {
        activeCameras.put(player, hand);
    }

}
