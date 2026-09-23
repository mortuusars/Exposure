package io.github.mortuusars.exposure.integration;

import io.github.mortuusars.exposure.integration.real_camera.RealCameraCompat;

public class ModCompatibilityClient {
    public static void init() {
        if (Mods.REAL_CAMERA.isLoaded()) {
            RealCameraCompat.init();
        }
    }
}
