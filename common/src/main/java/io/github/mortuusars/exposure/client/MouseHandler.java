package io.github.mortuusars.exposure.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.gui.ClientGUI;
import io.github.mortuusars.exposure.gui.screen.camera.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;

public class MouseHandler {
    private static final boolean[] heldMouseButtons = new boolean[12];

    public static boolean handleMouseButtonPress(int button, int action, int modifiers) {
        if (button >= 0 && button < heldMouseButtons.length)
            heldMouseButtons[button] = action == InputConstants.PRESS;

        if (Minecraft.getInstance().player != null && CameraInHand.isActive(Minecraft.getInstance().player)
                && ExposureClient.getViewfinderControlsKey().matchesMouse(button)
                && !(Minecraft.getInstance().screen instanceof ViewfinderControlsScreen)) {
            ClientGUI.openViewfinderControlsScreen();
            // Do not cancel the event to keep sneaking
        }

        return false;
    }

    public static boolean isMouseButtonHeld(int button) {
        return button >= 0 && button < heldMouseButtons.length && heldMouseButtons[button];
    }
}
