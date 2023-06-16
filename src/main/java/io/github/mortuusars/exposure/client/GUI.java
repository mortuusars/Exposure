package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.camera.ExposureScreen;
import net.minecraft.client.Minecraft;

public class GUI {
    public static void showExposureViewScreen(String exposureId) {
        Minecraft.getInstance().setScreen(new ExposureScreen(exposureId));
    }
}
