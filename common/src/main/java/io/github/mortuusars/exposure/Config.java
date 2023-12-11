package io.github.mortuusars.exposure;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.awt.*;

public class Config {
    public static class Common {
        @ExpectPlatform
        public static boolean CAMERA_SPYGLASS_SUPERZOOM() { throw new AssertionError(); }

        @ExpectPlatform
        public static int LIGHTROOM_BW_FILM_PRINT_TIME() { throw new AssertionError(); }
        @ExpectPlatform
        public static int LIGHTROOM_COLOR_FILM_PRINT_TIME() { throw new AssertionError(); }
        @ExpectPlatform
        public static int LIGHTROOM_EXPERIENCE_PER_PRINT() { throw new AssertionError(); }
    }

    public static class Client {
        // UI
        @ExpectPlatform
        public static boolean CAMERA_SHOW_OPEN_WITH_SNEAK_IN_TOOLTIP() { throw new AssertionError(); }
        @ExpectPlatform
        public static boolean PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP() { throw new AssertionError(); }
        @ExpectPlatform
        public static boolean PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR() { throw new AssertionError(); }

        // CAPTURE
        @ExpectPlatform
        public static int FLASH_CAPTURE_DELAY_TICKS() { throw new AssertionError(); }

        // VIEWFINDER
        @ExpectPlatform
        public static double VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER() { throw new AssertionError(); }
        @ExpectPlatform
        public static int VIEWFINDER_BACKGROUND_COLOR() { throw new AssertionError(); }
        @ExpectPlatform
        public static int VIEWFINDER_FONT_MAIN_COLOR() { throw new AssertionError(); }
        @ExpectPlatform
        public static int VIEWFINDER_FONT_SECONDARY_COLOR() { throw new AssertionError(); }

        // IMAGE SAVING
        @ExpectPlatform
        public static boolean EXPOSURE_SAVING() { throw new AssertionError(); }
        @ExpectPlatform
        public static boolean EXPOSURE_SAVING_LEVEL_SUBFOLDER() { throw new AssertionError(); }

        public static int getColorFromHex(String hexColor) {
            return new Color((int)Long.parseLong(hexColor.replace("#", ""), 16), true).getRGB();
        }
    }
}
