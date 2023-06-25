package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundSyncCameraPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ClientOnlyLogic {
    public static void updateAndSendCameraStack(ItemStack cameraStack, InteractionHand hand) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            Exposure.LOGGER.error("Cannot update camera: player is null.");
            return;
        }

        player.setItemInHand(hand, cameraStack);

        int slot = player.getInventory().findSlotMatchingItem(cameraStack);

        if (slot == -1) {
            Exposure.LOGGER.error("Cannot send camera to the server: matching slot in player's inventory is not found.");
            return;
        }

        ViewfinderRenderer.update();
        Packets.sendToServer(new ServerboundSyncCameraPacket(cameraStack, hand));
    }
}
