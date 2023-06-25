package io.github.mortuusars.exposure.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderNew;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderOld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class Keyboard {
    public static boolean handleKeyPress(long windowId, int key, int scanCode, int action, int modifiers) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (Camera.getViewfinder().isActive(player) &&
                (key == InputConstants.KEY_ESCAPE || Minecraft.getInstance().options.keyInventory.matches(key, scanCode))) {
            Camera.getViewfinder().deactivate(player);
            return true;
        }

        return false;
    }
}
