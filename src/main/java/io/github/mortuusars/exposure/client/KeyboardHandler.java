package io.github.mortuusars.exposure.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.camera.CameraHelper;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.client.gui.screen.ViewfinderControlsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class KeyboardHandler {
    public static boolean handleViewfinderKeyPress(long windowId, int key, int scanCode, int action, int modifiers) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return false;

        if (key == InputConstants.KEY_ESCAPE || Minecraft.getInstance().options.keyInventory.matches(key, scanCode)) {
            if (action == 0) { // Release
                if (Minecraft.getInstance().screen instanceof ViewfinderControlsScreen viewfinderControlsScreen) {
                        viewfinderControlsScreen.onClose();
                }
                else {
                    CameraHelper.deactivateAll(player, true);
                }
            }
            return true;
        }

        if (Minecraft.getInstance().options.keyShift.matches(key, scanCode) &&
            !(Minecraft.getInstance().screen instanceof ViewfinderControlsScreen)) {
            ClientGUI.openViewfinderConfigScreen();
            return false; // Do not handle to keep sneaking
        }

        return false;
    }
}
