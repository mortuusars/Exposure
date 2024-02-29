package io.github.mortuusars.exposure;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.camera.infrastructure.FocalRange;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Using ForgeConfigApiPort on fabric allows using forge config in both environments and without extra dependencies on forge.
 */
public class Config {
    public static void loading(ModConfig.Type type) {
        update(type);
    }

    public static void reloading(ModConfig.Type type) {
        update(type);
    }

    private static void update(ModConfig.Type type) {
        if (type == ModConfig.Type.COMMON)
            Common.update();
    }

    public static class Common {
        public static final ForgeConfigSpec SPEC;

        // Camera
        public static final ForgeConfigSpec.ConfigValue<String> CAMERA_DEFAULT_FOCAL_RANGE;
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CAMERA_LENS_FOCAL_RANGES;
        public static Map<Item, FocalRange> CAMERA_LENSES = new HashMap<>();

        // Lightroom
        public static final ForgeConfigSpec.IntValue LIGHTROOM_BW_FILM_PRINT_TIME;
        public static final ForgeConfigSpec.IntValue LIGHTROOM_COLOR_FILM_PRINT_TIME;
        public static final ForgeConfigSpec.IntValue LIGHTROOM_EXPERIENCE_PER_PRINT;

        // Photographs
        public static final ForgeConfigSpec.IntValue STACKED_PHOTOGRAPHS_MAX_SIZE;

        // Compat
        public static final ForgeConfigSpec.BooleanValue CREATE_SPOUT_DEVELOPING_ENABLED;
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CREATE_SPOUT_DEVELOPING_SEQUENCE_COLOR;
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CREATE_SPOUT_DEVELOPING_SEQUENCE_BW;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            builder.push("Camera");
            {
                CAMERA_DEFAULT_FOCAL_RANGE = builder
                        .comment("Default focal range of the camera (with built in lens).",
                                "Separated by a comma. Allowed range: " + FocalRange.ALLOWED_MIN + "-" + FocalRange.ALLOWED_MAX,
                                "Default: 18-55")
                        .define("DefaultFocalRange", "18-55");

                CAMERA_LENS_FOCAL_RANGES = builder
                        .comment("Focal Range per lens. Item ID and min-max (or single number for primes) focal lengths. " +
                                    "Separated by a comma. Allowed range: " + FocalRange.ALLOWED_MIN + "-" + FocalRange.ALLOWED_MAX,
                                "Note: to attach the custom lens to the camera - it needs to be added to '#exposure:lenses' item tag.",
                                "Default: [\"minecraft:spyglass,55-200\"]")
                        .defineListAllowEmpty(List.of("LensFocalRanges"), () ->
                                List.of("minecraft:spyglass,55-200"), Common::validateLensProperties);
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

            builder.push("Photographs");
            {
                STACKED_PHOTOGRAPHS_MAX_SIZE = builder
                        .comment("How many photographs can be stacked in Stacked Photographs item. Default: 16.",
                                "Larger numbers may cause errors. Use at your own risk.")
                        .defineInRange("StackedPhotographsMaxSize", 16, 2, 64);
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
                        CREATE_SPOUT_DEVELOPING_SEQUENCE_COLOR = builder
                                .comment("Fluid spouting sequence required to develop color film.")
                                .defineList("ColorFilmSequence", PlatformHelper.getDefaultSpoutDevelopmentColorSequence(), o -> true);
                        CREATE_SPOUT_DEVELOPING_SEQUENCE_BW = builder
                                .comment("Fluid spouting sequence required to develop black and white film.")
                                .defineList("BlackAndWhiteFilmSequence", PlatformHelper.getDefaultSpoutDevelopmentBWSequence(), o -> true);
                    }
                    builder.pop();
                }
                builder.pop();
            }
            builder.pop();

            SPEC = builder.build();
        }

        private static boolean validateLensProperties(Object o) {
            String value = (String) o;
            try {
                @SuppressWarnings("unused") Pair<Item, FocalRange> unused = parseLensFocalRange(value);
                return true;
            } catch (Exception e) {
                LogUtils.getLogger().error("Lens property '" + value + "' is not a valid. " + e);
                return false;
            }
        }

        private static Pair<Item, FocalRange> parseLensFocalRange(String value) {
            String[] split = value.split(",");
            if (split.length != 2)
                throw new IllegalStateException(value + " is not a valid lens property. Exactly two parts, separated by a comma, are required.");

            ResourceLocation id = new ResourceLocation(split[0]);
            Item item = BuiltInRegistries.ITEM.get(id);

            if (item == Items.AIR)
                throw new IllegalStateException(item + " is not a valid item for lens property. Value: " + value);

            FocalRange focalRange = FocalRange.parse(split[1]);

            return Pair.of(item, focalRange);
        }

        public static ForgeConfigSpec.ConfigValue<List<? extends String>> spoutDevelopingSequence(FilmType filmType) {
            return filmType == FilmType.COLOR ? CREATE_SPOUT_DEVELOPING_SEQUENCE_COLOR : CREATE_SPOUT_DEVELOPING_SEQUENCE_BW;
        }

        public static void update() {
            List<? extends String> strings = CAMERA_LENS_FOCAL_RANGES.get();
            for (String value : strings) {
                Pair<Item, FocalRange> lens = parseLensFocalRange(value);
                CAMERA_LENSES.put(lens.getFirst(), lens.getSecond());
            }

            LogUtils.getLogger().info("Exposure: Config updated.\n"
                    + "Camera Lenses: " + CAMERA_LENSES.toString());
        }
    }

    public static class Client {
        public static final ForgeConfigSpec SPEC;

        // UI
        public static final ForgeConfigSpec.BooleanValue RECIPE_TOOLTIPS_WITHOUT_JEI;
        public static final ForgeConfigSpec.BooleanValue CAMERA_SHOW_OPEN_WITH_SNEAK_IN_TOOLTIP;
        public static final ForgeConfigSpec.BooleanValue PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP;
        public static final ForgeConfigSpec.BooleanValue PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR;
        public static final ForgeConfigSpec.BooleanValue SIGNED_ALBUM_GLINT;
        public static final ForgeConfigSpec.BooleanValue ALBUM_SHOW_PHOTOS_COUNT;

        // CAPTURE
        public static final ForgeConfigSpec.IntValue FLASH_CAPTURE_DELAY_TICKS;

        // VIEWFINDER
        public static final ForgeConfigSpec.DoubleValue VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER;
        public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_BACKGROUND_COLOR;
        public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_FONT_MAIN_COLOR;
        public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_FONT_SECONDARY_COLOR;

        // IMAGE SAVING
        public static final ForgeConfigSpec.BooleanValue SAVE_EXPOSURE_TO_FILE_WHEN_VIEWED;
        public static final ForgeConfigSpec.BooleanValue EXPOSURE_SAVING_LEVEL_SUBFOLDER;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            {
                builder.push("UI");

                RECIPE_TOOLTIPS_WITHOUT_JEI = builder
                        .comment("Tooltips for Developing Film Rolls and Copying Photographs will be shown on Film Rolls and Photographs respectively, describing the crafting recipe. ",
                                "Only when JEI is not installed. (Only JEI shows these recipes, not REI or EMI)")
                        .define("RecipeTooltipsWithoutJei", true);

                CAMERA_SHOW_OPEN_WITH_SNEAK_IN_TOOLTIP = builder
                        .comment("'Use while sneaking to open' message will be shown in Camera item tooltip.")
                        .define("CameraSneakOpenTooltip", true);

                PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP = builder
                        .comment("Photographer name will be shown in Photograph's tooltip.")
                        .define("PhotographPhotographerNameTooltip", false);

                PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR = builder
                        .comment("Crosshair will not get in the way when holding a photograph.")
                        .define("PhotographInHandHideCrosshair", true);

                ALBUM_SHOW_PHOTOS_COUNT = builder
                        .comment("Album will show how many photographs they contain in a tooltip.")
                        .define("AlbumShowPhotosCount", true);

                SIGNED_ALBUM_GLINT = builder
                        .comment("Signed Album item will have an enchantment glint.")
                        .define("SignedAlbumGlint", true);


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
                SAVE_EXPOSURE_TO_FILE_WHEN_VIEWED = builder
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
