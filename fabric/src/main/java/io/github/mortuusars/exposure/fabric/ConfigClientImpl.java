package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Config;

public class ConfigClientImpl {
    // UI
    public static boolean CAMERA_SHOW_OPEN_WITH_SNEAK_IN_TOOLTIP() {
        return true;
    }

    public static boolean PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP() {
        return false;
    }

    public static boolean PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR() {
        return true;
    }

    // CAPTURE
    public static int FLASH_CAPTURE_DELAY_TICKS() {
        return 3;
    }

    // VIEWFINDER
    public static double VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER() {
        return 0.048;
    }

    public static int VIEWFINDER_BACKGROUND_COLOR() {
        return Config.Client.getColorFromHex("FA1F1D1B");
    }

    public static int VIEWFINDER_FONT_MAIN_COLOR() {
        return Config.Client.getColorFromHex("FF2B2622");
    }

    public static int VIEWFINDER_FONT_SECONDARY_COLOR() {
        return Config.Client.getColorFromHex("FF7A736C");
    }

    // IMAGE SAVING
    public static boolean EXPOSURE_SAVING() {
        return true;
    }

    public static boolean EXPOSURE_SAVING_LEVEL_SUBFOLDER() {
        return true;
    }
}
