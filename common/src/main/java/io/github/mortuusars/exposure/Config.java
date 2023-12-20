package io.github.mortuusars.exposure;

import net.minecraftforge.common.ForgeConfigSpec;

import java.awt.*;
import java.util.List;

/**
 * Using ForgeConfigApiPort on fabric allows using forge config in both environments and without extra dependencies on forge.
 */
public class Config {
    public static class Common {
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.BooleanValue CAMERA_SPYGLASS_SUPERZOOM;

        public static final ForgeConfigSpec.IntValue LIGHTROOM_BW_FILM_PRINT_TIME;
        public static final ForgeConfigSpec.IntValue LIGHTROOM_COLOR_FILM_PRINT_TIME;
        public static final ForgeConfigSpec.IntValue LIGHTROOM_EXPERIENCE_PER_PRINT;

        // Compat
        public static final ForgeConfigSpec.BooleanValue CREATE_SPOUT_DEVELOPING_ENABLED;
        public static final ForgeConfigSpec.ConfigValue<List<String>> CREATE_SPOUT_DEVELOPING_STEPS_COLOR;
        public static final ForgeConfigSpec.ConfigValue<List<String>> CREATE_SPOUT_DEVELOPING_STEPS_BW;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            builder.push("Camera");
            {
                CAMERA_SPYGLASS_SUPERZOOM = builder
                        .comment("Spyglass will function like a superzoom lens instead of a teleconverter, allowing for a full range of focal lengths (18-200).",
                                "Using it as a teleconverter allows only 55-200")
                        .define("SpyglassSuperzoom", false);
            }
            builder.pop();

            builder.push("Lightroom");
            {
                LIGHTROOM_BW_FILM_PRINT_TIME = builder
                        .comment("Time in ticks to print black and white photograph.")
                        .defineInRange("BlackAndWhitePrintTime", 80, 1, Integer.MAX_VALUE);
                LIGHTROOM_COLOR_FILM_PRINT_TIME = builder
                        .comment("Time in ticks to print color photograph.")
                        .defineInRange("ColorPrintTime", 200, 1, Integer.MAX_VALUE);
                LIGHTROOM_EXPERIENCE_PER_PRINT = builder
                        .comment("Amount of experience awarded per printed Photograph. Set to 0 to disable.")
                        .defineInRange("ExperiencePerPrint", 4, 0, 32767);
            }
            builder.pop();

            builder.push("Integration");
            {
                builder.push("Create");
                {
                    builder.push("SequencedSpoutFilmDeveloping");
                    {
                        CREATE_SPOUT_DEVELOPING_ENABLED = builder
                                .comment("Film can be developed with create Spout Filling. Default: true")
                                .define("Enabled", true);
                        CREATE_SPOUT_DEVELOPING_STEPS_COLOR = builder
                                .comment("Fluid spouting steps required to develop color film.")
                                .define("ColorFilmSteps", PlatformHelper.getDefaultSpoutDevelopmentColorSteps());
                        CREATE_SPOUT_DEVELOPING_STEPS_BW = builder
                                .comment("Fluid spouting steps required to develop black and white film.")
                                .define("BlackAndWhiteFilmSteps", PlatformHelper.getDefaultSpoutDevelopmentBWSteps());
                    }
                    builder.pop();
                }
                builder.pop();
            }
            builder.pop();

            SPEC = builder.build();
        }
    }

    public static class Client {
        public static final ForgeConfigSpec SPEC;

        // UI
        public static final ForgeConfigSpec.BooleanValue CAMERA_SHOW_OPEN_WITH_SNEAK_IN_TOOLTIP;
        public static final ForgeConfigSpec.BooleanValue PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP;
        public static final ForgeConfigSpec.BooleanValue PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR;

        // CAPTURE
        public static final ForgeConfigSpec.IntValue FLASH_CAPTURE_DELAY_TICKS;

        // VIEWFINDER
        public static final ForgeConfigSpec.DoubleValue VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER;
        public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_BACKGROUND_COLOR;
        public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_FONT_MAIN_COLOR;
        public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_FONT_SECONDARY_COLOR;

        // IMAGE SAVING
        public static final ForgeConfigSpec.BooleanValue EXPOSURE_SAVING;
        public static final ForgeConfigSpec.BooleanValue EXPOSURE_SAVING_LEVEL_SUBFOLDER;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            {
                builder.push("UI");

                CAMERA_SHOW_OPEN_WITH_SNEAK_IN_TOOLTIP = builder
                        .comment("'Use while sneaking to open' message will be shown in Camera item tooltip.")
                        .define("CameraSneakOpenTooltip", true);

                PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP = builder
                        .comment("Photographer name will be shown in Photograph's tooltip.")
                        .define("PhotographPhotographerNameTooltip", false);

                PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR = builder
                        .comment("Crosshair will not get in the way when holding a photograph.")
                        .define("PhotographInHandHideCrosshair", true);

                {
                    builder.push("Viewfinder");
                    VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER = builder
                            .comment("Mouse sensitivity modifier per 5 degrees of fov. Set to 0 to disable sensitivity changes.")
                            .defineInRange("ZoomSensitivityModifier", 0.048, 0.0, 1.0);
                    VIEWFINDER_BACKGROUND_COLOR = builder.define("BackgroundColorHex", "FA1F1D1B");
                    VIEWFINDER_FONT_MAIN_COLOR = builder.define("FontMainColorHex", "FF2B2622");
                    VIEWFINDER_FONT_SECONDARY_COLOR = builder.define("FontSecondaryColorHex", "FF7A736C");
                    builder.pop();
                }

                builder.pop();
            }

            {
                builder.push("Capture");
                FLASH_CAPTURE_DELAY_TICKS = builder
                        .comment("Delay in ticks before capturing an image when shooting with flash." +
                                "\nIf you experience flash synchronization issues (Flash having no effect on the image) - try increasing the value.")
                        .defineInRange("FlashCaptureDelayTicks", 3, 1, 6);
                builder.pop();
            }

            {
                builder.push("FileSaving");
                EXPOSURE_SAVING = builder
                        .comment("When the Photograph is viewed in UI, image will be saved to 'exposures' folder as a png.")
                        .define("SavePhotographs", true);
                EXPOSURE_SAVING_LEVEL_SUBFOLDER = builder
                        .comment("When saving, exposures will be organized into a folders corresponding to current world name.")
                        .define("WorldNameSubfolder", true);
                builder.pop();
            }

            SPEC = builder.build();
        }

        public static int getBackgroundColor() {
            return getColorFromHex(VIEWFINDER_BACKGROUND_COLOR.get());
        }

        public static int getMainFontColor() {
            return getColorFromHex(VIEWFINDER_FONT_MAIN_COLOR.get());
        }

        public static int getSecondaryFontColor() {
            return getColorFromHex(VIEWFINDER_FONT_SECONDARY_COLOR.get());
        }

        private static int getColorFromHex(String hexColor) {
            return new Color((int) Long.parseLong(hexColor.replace("#", ""), 16), true).getRGB();
        }
    }
}
