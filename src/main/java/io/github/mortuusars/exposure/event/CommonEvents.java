package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

public class CommonEvents {
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        Exposure.getCamera().tick(event.player);
    }

    public static void entityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        if (Exposure.getCamera().isActive(player)) {
            event.setCanceled(true); //TODO: This half-works. Shot is taken, but interaction still proceeds.
            CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
            camera.getItem().use(player.level, player, camera.getHand());
        }
    }
}
