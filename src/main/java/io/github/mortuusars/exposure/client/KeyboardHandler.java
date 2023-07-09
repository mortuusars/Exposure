package io.github.mortuusars.exposure.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.viewfinder.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.UpdateActiveCameraPacket;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class KeyboardHandler {
    public static boolean handleKeyPress(long windowId, int key, int scanCode, int action, int modifiers) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || !Exposure.getCamera().isActive(player))
            return false;

        if (key == InputConstants.KEY_ESCAPE || Minecraft.getInstance().options.keyInventory.matches(key, scanCode)) {
            if (action == 0) { // Release
                if (Minecraft.getInstance().screen instanceof ViewfinderControlsScreen viewfinderControlsScreen) {
                        viewfinderControlsScreen.onClose();
                }
                else {
                    Exposure.getCamera().deactivate(player);
                    CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
                    Packets.sendToServer(new UpdateActiveCameraPacket(player.getUUID(), false, camera.getHand()));
                }
            }
            return true;
        }

        if (Minecraft.getInstance().options.keySprint.matches(key, scanCode) &&
            !(Minecraft.getInstance().screen instanceof ViewfinderControlsScreen)) {
            GUI.showViewfinderConfigScreen();
            return true;
        }

        return false;
    }
}
