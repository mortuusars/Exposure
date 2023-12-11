package io.github.mortuusars.exposure.forge;

import io.github.mortuusars.exposure.Config;

public class ConfigClientImpl {
    // UI
    public static boolean CAMERA_SHOW_OPEN_WITH_SNEAK_IN_TOOLTIP() {
        return ConfigForge.Client.CAMERA_SHOW_OPEN_WITH_SNEAK_IN_TOOLTIP.get();
    }

    public static boolean PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP() {
        return ConfigForge.Client.PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP.get();
    }

    public static boolean PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR() {
        return ConfigForge.Client.PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR.get();
    }

    // CAPTURE
    public static int FLASH_CAPTURE_DELAY_TICKS() {
        return ConfigForge.Client.FLASH_CAPTURE_DELAY_TICKS.get();
    }

    // VIEWFINDER
    public static double VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER() {
        return ConfigForge.Client.VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER.get();
    }

    public static int VIEWFINDER_BACKGROUND_COLOR() {
        return Config.Client.getColorFromHex(ConfigForge.Client.VIEWFINDER_BACKGROUND_COLOR.get());
    }

    public static int VIEWFINDER_FONT_MAIN_COLOR() {
        return Config.Client.getColorFromHex(ConfigForge.Client.VIEWFINDER_FONT_MAIN_COLOR.get());
    }

    public static int VIEWFINDER_FONT_SECONDARY_COLOR() {
        return Config.Client.getColorFromHex(ConfigForge.Client.VIEWFINDER_FONT_SECONDARY_COLOR.get());
    }

    // IMAGE SAVING
    public static boolean EXPOSURE_SAVING() {
        return ConfigForge.Client.EXPOSURE_SAVING.get();
    }

    public static boolean EXPOSURE_SAVING_LEVEL_SUBFOLDER() {
        return ConfigForge.Client.EXPOSURE_SAVING_LEVEL_SUBFOLDER.get();
    }
}
