package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.component.Shutter;
import io.github.mortuusars.exposure.client.viewfinder.ViewfinderRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class ClientCamera extends Camera {
    public ClientCamera(Shutter shutter) {
        super(shutter);
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public void activate(Player player, InteractionHand hand) {
        super.activate(player, hand);
        ViewfinderRenderer.setup();
    }
}
