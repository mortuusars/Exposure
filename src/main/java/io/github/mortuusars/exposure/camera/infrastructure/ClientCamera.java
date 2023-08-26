package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.client.render.ViewfinderRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

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

        if (player.equals(Minecraft.getInstance().player))
            ViewfinderRenderer.setup();
    }

    @Override
    public @Nullable InteractionHand deactivate(Player player) {
        player.stopUsingItem();
        if (player.equals(Minecraft.getInstance().player))
            ViewfinderRenderer.teardown();

        return super.deactivate(player);
    }
}
