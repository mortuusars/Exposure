package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.DeactivateCameraServerboundPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CameraHelper {
    public static void deactivateAll(Player player, boolean sendToServer) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof CameraItem cameraItem)
                deactivate(player, cameraItem, stack);
        }

        if (player.getOffhandItem().getItem() instanceof CameraItem cameraItem)
            deactivate(player, cameraItem, player.getOffhandItem());

        if (player.getLevel().isClientSide) {
            ViewfinderClient.close(player);

            if (sendToServer)
                Packets.sendToServer(new DeactivateCameraServerboundPacket(player.getUUID()));
        }
    }

    public static void deactivate(Player player, CameraItem item, ItemStack stack) {
        if (item.isActive(player, stack))
            item.deactivate(player, stack);
        else
            item.setActive(player, stack, false);
    }
}
