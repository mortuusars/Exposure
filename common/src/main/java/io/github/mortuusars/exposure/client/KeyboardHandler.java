package io.github.mortuusars.exposure.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.camera.infrastructure.ZoomDirection;
import io.github.mortuusars.exposure.camera.viewfinder.SelfieClient;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.gui.ClientGUI;
import io.github.mortuusars.exposure.gui.screen.camera.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public class KeyboardHandler {
    public static boolean handleViewfinderKeyPress(long windowId, int key, int scanCode, int action, int modifiers) {
        Minecraft minecraft = Minecraft.getInstance();
        @Nullable LocalPlayer player = minecraft.player;

        if (player == null || !CameraInHand.isActive(player))
            return false;

        if (minecraft.options.keyTogglePerspective.matches(key, scanCode)) {
            if (action == InputConstants.PRESS)
                return true;

            CameraType currentCameraType = minecraft.options.getCameraType();
            CameraType newCameraType = currentCameraType == CameraType.FIRST_PERSON ? CameraType.THIRD_PERSON_FRONT
                    : CameraType.FIRST_PERSON;

            minecraft.options.setCameraType(newCameraType);

            CameraInHand camera = CameraInHand.getActive(player);

            SelfieClient.update(camera.getCamera(), camera.getHand(), true);

            return true;
        }


        if (key == InputConstants.KEY_ESCAPE || minecraft.options.keyInventory.matches(key, scanCode)) {
            if (action == 0) { // Release
                if (minecraft.screen instanceof ViewfinderControlsScreen viewfinderControlsScreen)
                    viewfinderControlsScreen.onClose();
                else
                    CameraInHand.deactivate(player);
            }
            return true;
        }

        if (!ViewfinderClient.isLookingThrough())
            return false;

        if (!(minecraft.screen instanceof ViewfinderControlsScreen)) {
            if (minecraft.options.keyShift.matches(key, scanCode)) {
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
