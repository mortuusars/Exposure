package io.github.mortuusars.exposure.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.awt.*;

public class Config {
    public static class Common {
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.IntValue LIGHTROOM_BW_FILM_PRINT_TIME;
        public static final ForgeConfigSpec.IntValue LIGHTROOM_COLOR_FILM_PRINT_TIME;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            builder.push("Lightroom");

            LIGHTROOM_BW_FILM_PRINT_TIME = builder
                    .comment("Time in ticks to print black and white photograph.")
                    .defineInRange("BlackAndWhitePrintTime", 100, 1, Integer.MAX_VALUE);
            LIGHTROOM_COLOR_FILM_PRINT_TIME = builder
                    .comment("Time in ticks to print color photograph.")
                    .defineInRange("ColorPrintTime", 300, 1, Integer.MAX_VALUE);

            builder.pop();

            SPEC = builder.build();
        }
    }

    public static class Client {
        public static final ForgeConfigSpec SPEC;

        // VIEWFINDER
        public static final ForgeConfigSpec.DoubleValue VIEWFINDER_CROP_FACTOR;
        public static final ForgeConfigSpec.DoubleValue VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER;
        public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_BACKGROUND_COLOR;
        public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_FONT_MAIN_COLOR;
        public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_FONT_SECONDARY_COLOR;

        // IMAGE SAVING
        public static final ForgeConfigSpec.ConfigValue<String> EXPOSURE_SAVE_PATH;
        public static final ForgeConfigSpec.BooleanValue EXPOSURE_SAVE_LEVEL_SUBFOLDER;
        public static final ForgeConfigSpec.BooleanValue EXPOSURE_SAVE_ON_EVERY_CAPTURE;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            builder.push("ViewfinderUI");
            VIEWFINDER_CROP_FACTOR = builder
                    .comment("Size of the texture relative to the size of the opening. Default: 256 / (256 - 16*2) = 1.142857")
                    .defineInRange("CropFactor", 1.142857, 1.0, 10.0);
            VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER = builder
                    .comment("Mouse sensitivity modifier per 5 degrees of fov. Set to 0 to disable sensitivity changes. Default: 0.048")
                    .defineInRange("ZoomSensitivityModifier", 0.048, 0.0, 1.0);
            VIEWFINDER_BACKGROUND_COLOR = builder.define("BackgroundColorHex", "FA1F1D1B");
            VIEWFINDER_FONT_MAIN_COLOR = builder.define("FontMainColorHex", "FF2B2622");
            VIEWFINDER_FONT_SECONDARY_COLOR = builder.define("FontSecondaryColorHex", "FF7A736C");
            builder.pop();

            builder.push("ExposureFileSaving");
            EXPOSURE_SAVE_PATH = builder.define("FolderPath", "exposures");
            EXPOSURE_SAVE_LEVEL_SUBFOLDER = builder.define("PutInLevelSubfolder", true);
            EXPOSURE_SAVE_ON_EVERY_CAPTURE = builder.define("SaveOnEveryCapture", true); //TODO: remove before release
            builder.pop();

            SPEC = builder.build();
        }

        public static int getBackgroundColor() {
            String value = VIEWFINDER_BACKGROUND_COLOR.get();
            value = value.replace("#", "");
            return new Color((int)Long.parseLong(value, 16), true).getRGB();
        }

        public static int getMainFontColor() {
            String value = VIEWFINDER_FONT_MAIN_COLOR.get();
            value = value.replace("#", "");
            return new Color((int)Long.parseLong(value, 16), true).getRGB();
        }

        public static int getSecondaryFontColor() {
            String value = VIEWFINDER_FONT_SECONDARY_COLOR.get();
            value = value.replace("#", "");
            return new Color((int)Long.parseLong(value, 16), true).getRGB();
        }

    }
}
