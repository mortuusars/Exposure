package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import net.minecraft.client.input.MouseButtonEvent;

public class MouseHandler {
    private static final boolean[] heldMouseButtons = new boolean[12];

    public static boolean isMouseButtonHeld(int button) {
        return button >= 0 && button < heldMouseButtons.length && heldMouseButtons[button];
    }

    public static boolean buttonPressed(int button, int action, int modifiers, MouseButtonEvent mouseButtonEvent) {
        if (button >= 0 && button < heldMouseButtons.length)
            heldMouseButtons[button] = action == InputConstants.PRESS;

        return CameraClient.viewfinder() != null && CameraClient.viewfinder().mouseClicked(mouseButtonEvent);
    }

    public static boolean scrolled(double amount) {
        return CameraClient.viewfinder() != null && CameraClient.viewfinder().mouseScrolled(amount);
    }

    public static double modifySensitivity(double original) {
        return CameraClient.viewfinder() != null ? CameraClient.viewfinder().modifyMouseSensitivity(original) : original;
    }

    public static boolean onTurnPlayer(double xRot, double yRot) {
        return CameraClient.viewfinder() != null
                && CameraClient.viewfinder().isLookingThrough()
                && CameraClient.viewfinder().selfie().mouseMove(xRot, yRot);
    }
}
