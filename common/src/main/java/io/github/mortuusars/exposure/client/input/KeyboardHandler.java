package io.github.mortuusars.exposure.client.input;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class KeyboardHandler {
    private static final KeyMapping.Category EXPOSURE_CATEGORY =
            KeyMapping.Category.register(Exposure.resource("exposure.controls"));

    @Nullable
    private static KeyMapping openCameraControlsKey = null;

    public static void registerKeymappings(Function<KeyMapping, KeyMapping> registerFunction) {
        KeyMapping keyMapping = new KeyMapping("key.exposure.camera_controls",
                InputConstants.UNKNOWN.getValue(), EXPOSURE_CATEGORY);

        openCameraControlsKey = registerFunction.apply(keyMapping);
    }

    public static boolean handleKeyPress(long windowId, int key, int scanCode, int action, int modifiers) {
        return Minecrft.get().player != null
                && CameraClient.viewfinder() != null
                && CameraClient.viewfinder().keyPressed(key, scanCode, action, modifiers);
    }

    public static KeyMapping getCameraControlsKey() {
        Preconditions.checkState(openCameraControlsKey != null,
                "Viewfinder Controls key mapping was not registered");

        return openCameraControlsKey.isUnbound() ? Minecraft.getInstance().options.keyShift : openCameraControlsKey;
    }
}
