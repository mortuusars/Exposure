package io.github.mortuusars.exposure.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import net.minecraft.client.Minecraft;

public class Keyboard {
    public static boolean handleKeyPress(long windowId, int key, int scanCode, int action, int modifiers) {
        if (Viewfinder.isActive() &&
                (key == InputConstants.KEY_ESCAPE || Minecraft.getInstance().options.keyInventory.matches(key, scanCode))) {
            Viewfinder.setActive(false);
            return true;
        }

        return false;
    }
}
