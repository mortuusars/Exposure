package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.DeactivateCameraServerboundPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CameraHelper {
    public static void deactivate(Player player, boolean sendToServer) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof CameraItem cameraItem)
                cameraItem.deactivate(player, stack);
        }

        if (player.getOffhandItem().getItem() instanceof CameraItem cameraItem)
            cameraItem.deactivate(player, player.getOffhandItem());

        if (player.getLevel().isClientSide) {
            ViewfinderClient.close(player);

            if (sendToServer)
                Packets.sendToServer(new DeactivateCameraServerboundPacket(player.getUUID()));
        }
    }
}
