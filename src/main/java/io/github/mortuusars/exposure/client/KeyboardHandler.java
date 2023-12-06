package io.github.mortuusars.exposure.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.camera.infrastructure.ZoomDirection;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.client.gui.screen.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public class KeyboardHandler {
    public static boolean handleViewfinderKeyPress(long windowId, int key, int scanCode, int action, int modifiers) {
        @Nullable LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || !CameraInHand.isActive(player) || !ViewfinderClient.isLookingThrough())
            return false;

        if (key == InputConstants.KEY_ESCAPE || Minecraft.getInstance().options.keyInventory.matches(key, scanCode)) {
            if (action == 0) { // Release
                if (Minecraft.getInstance().screen instanceof ViewfinderControlsScreen viewfinderControlsScreen)
                    viewfinderControlsScreen.onClose();
                else
                    CameraInHand.deactivate(player);
            }
            return true;
        }

        if (!(Minecraft.getInstance().screen instanceof ViewfinderControlsScreen)) {
            if (Minecraft.getInstance().options.keyShift.matches(key, scanCode)) {
                ClientGUI.openViewfinderControlsScreen();
                return false; // Do not handle to keep sneaking
            }

            if (action == 1 || action == 2) { // Press or Hold
                if (key == InputConstants.KEY_ADD || key == InputConstants.KEY_EQUALS) {
                    ViewfinderClient.zoom(ZoomDirection.IN, false);
                    return true;
                }

                if (key == 333 /*KEY_SUBTRACT*/ || key == InputConstants.KEY_MINUS) {
                    ViewfinderClient.zoom(ZoomDirection.OUT, false);
                    return true;
                }
            }
        }

        return false;
    }
}
