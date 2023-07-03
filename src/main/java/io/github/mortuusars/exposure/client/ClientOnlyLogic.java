package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundSyncCameraPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ClientOnlyLogic {
    public static void updateAndSyncCameraStack(ItemStack cameraStack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            Exposure.LOGGER.error("Cannot update camera: player is null.");
            return;
        }

        InteractionHand hand = player.getMainHandItem().getItem() instanceof CameraItem ?
                InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        player.setItemInHand(hand, cameraStack);
        Camera.updateActiveCamera(cameraStack);

        Packets.sendToServer(new ServerboundSyncCameraPacket(cameraStack, hand));
    }
}
