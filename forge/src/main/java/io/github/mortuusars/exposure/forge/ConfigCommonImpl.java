package io.github.mortuusars.exposure.forge;

public class ConfigCommonImpl {
    public static boolean CAMERA_SPYGLASS_SUPERZOOM() {
        return ConfigForge.Common.CAMERA_SPYGLASS_SUPERZOOM.get();
    }

    public static int LIGHTROOM_BW_FILM_PRINT_TIME() {
        return ConfigForge.Common.LIGHTROOM_BW_FILM_PRINT_TIME.get();
    }

    public static int LIGHTROOM_COLOR_FILM_PRINT_TIME() {
        return ConfigForge.Common.LIGHTROOM_COLOR_FILM_PRINT_TIME.get();
    }

    public static int LIGHTROOM_EXPERIENCE_PER_PRINT() {
        return ConfigForge.Common.LIGHTROOM_EXPERIENCE_PER_PRINT.get();
    }
}
