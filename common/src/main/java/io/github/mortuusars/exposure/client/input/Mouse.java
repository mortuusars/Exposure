package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.network.packet.serverbound.ServerboundCameraStandTurnPacket;
import io.github.mortuusars.exposure.world.entity.stand.CameraStand;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.mortaar.network.Packets;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class Mouse {
    private static final boolean[] heldMouseButtons = new boolean[12];

    public static boolean isMouseButtonHeld(int button) {
        return button >= 0 && button < heldMouseButtons.length && heldMouseButtons[button];
    }

    public static boolean buttonPressed(int button, int action, int modifiers) {
        if (button >= 0 && button < heldMouseButtons.length)
            heldMouseButtons[button] = action == InputConstants.PRESS;

        return CameraClient.viewfinder() != null && CameraClient.viewfinder().mouseClicked(button, action);
    }

    public static boolean scrolled(double amount) {
        return CameraClient.viewfinder() != null && CameraClient.viewfinder().mouseScrolled(amount);
    }

    public static double modifySensitivity(double original) {
        return CameraClient.viewfinder() != null ? CameraClient.viewfinder().modifyMouseSensitivity(original) : original;
    }

    public static boolean onTurnPlayer(double yRot, double xRot) {
        if (CameraClient.viewfinder() != null
              && CameraClient.viewfinder().isLookingThrough()
              && CameraClient.viewfinder().selfie().turn(yRot, xRot)) {
            return true; // Prevent player turning
        }

        if (controlledStand != null) {
            if (controlledStand.isRemoved()) {
                setControlledStand(null);
                return false; // Allow turn
            }

            if (controlledStand.getCamera().getItem() instanceof CameraItem cameraItem) {
                if (controlledStand.getOperatorId() != Minecrft.playerOrThrow().getId()) {
                    setControlledStand(null);
                    return false; // Allow turn
                }
            }

            if (controlledStand.getCamera().is(Items.SPYGLASS)) {
                if (!Minecrft.options().keyUse.isDown()) {
                    setControlledStand(null);
                    CameraClient.resetCameraEntity();
                    return false; // Allow turn
                }
            }

//            if (!(controlledStand.getItem().getItem() instanceof CameraItem)) {
//                yRot *= -1;
////                xRot *= -1;
//            }

            controlledStand.turn(yRot, xRot);
            Packets.sendToServer(new ServerboundCameraStandTurnPacket(controlledStand.getId(), yRot, xRot));
            return true; // Prevent player turning
        }

//        if (Minecrft.playerOrThrow().getActiveExposureCamera() instanceof CameraOnStand cameraOnStand) {
//            CameraStandEntity stand = cameraOnStand.getStand();
//            stand.turn(yRot, xRot);
//            Packets.sendToServer(new ServerboundCameraStandTurnPacket(stand.getId(), yRot, xRot));
//            return true;
//        }

        return false; // Allow turn
    }

    private static @Nullable CameraStand controlledStand = null;

    public static @Nullable CameraStand getControlledStand() {
        return controlledStand;
    }

    public static void setControlledStand(@Nullable CameraStand stand) {
        controlledStand = stand;
    }
}
