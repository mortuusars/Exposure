package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.world.block.FlashBlock;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Using ForgeConfigApiPort on fabric allows using forge config in both environments and without extra dependencies on forge.
 */
public class Config {
    public static class Server {
        public static final ModConfigSpec SPEC;

        // Camera
        public static final ModConfigSpec.ConfigValue<String> CAMERA_DEFAULT_FOCAL_RANGE;
        public static final ModConfigSpec.BooleanValue CAMERA_VIEWFINDER_ATTACK;
        public static final ModConfigSpec.BooleanValue WAIST_LEVEL_VIEWFINDER;
        public static final ModConfigSpec.DoubleValue SELFIE_CAMERA_DISTANCE;
        public static final ModConfigSpec.BooleanValue CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS;
        public static final ModConfigSpec.BooleanValue CAMERA_GUI_RIGHT_CLICK_HOTSWAP;
        public static final ModConfigSpec.BooleanValue TIMER_ATTRACTS_MOB_ATTENTION;
        public static final ModConfigSpec.IntValue TIMER_ATTENTION_RADIUS;

        // Camera Stand
        public static final ModConfigSpec.IntValue CAMERA_STAND_WORKING_RANGE;
        public static final ModConfigSpec.BooleanValue CAMERA_STAND_RANGE_MALFUNCTION;
        public static final ModConfigSpec.BooleanValue CAMERA_STAND_FALLBACK_TO_OTHER_PLAYERS;

        // Capture
        public static final ModConfigSpec.IntValue DEFAULT_FRAME_SIZE;
        public static final ModConfigSpec.BooleanValue CAN_PROJECT;
        public static final ModConfigSpec.IntValue PROJECT_TIMEOUT_TICKS;

        // Lightroom
        public static final ModConfigSpec.IntValue LIGHTROOM_LIGHT_REQUIREMENT;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> LIGHTROOM_BW_DYES;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> LIGHTROOM_COLOR_DYES;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> LIGHTROOM_CHROMATIC_RED_DYES;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> LIGHTROOM_CHROMATIC_GREEN_DYES;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> LIGHTROOM_CHROMATIC_BLUE_DYES;
        public static final ModConfigSpec.IntValue LIGHTROOM_BW_PRINT_TIME;
        public static final ModConfigSpec.IntValue LIGHTROOM_COLOR_PRINT_TIME;
        public static final ModConfigSpec.IntValue LIGHTROOM_CHROMATIC_PRINT_TIME;
        public static final ModConfigSpec.IntValue LIGHTROOM_BW_EXPERIENCE;
        public static final ModConfigSpec.IntValue LIGHTROOM_COLOR_EXPERIENCE;
        public static final ModConfigSpec.IntValue LIGHTROOM_CHROMATIC_EXPERIENCE;

        // Photographs
        public static final ModConfigSpec.IntValue STACKED_PHOTOGRAPHS_MAX_SIZE;

        // Misc
        public static final ModConfigSpec.BooleanValue FILM_ROLL_EASY_RENAMING;
        public static final ModConfigSpec.BooleanValue INTERPLANAR_PROJECTOR_LARGER_RENAMING_LIMIT;

        // Debug
        public static final ModConfigSpec.BooleanValue CLEANUP_TIMED_OUT_EXPECTED_EXPOSURES;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            {
                builder.push("camera");
                CAMERA_DEFAULT_FOCAL_RANGE = builder
                        .comment("Default focal range of the camera (without a lens attached).",
                                "Allowed range: " + FocalRange.ALLOWED_MIN + "-" + FocalRange.ALLOWED_MAX,
                                "Default: 18-55")
                        .define("default_focal_range", "18-55");
                CAMERA_VIEWFINDER_ATTACK = builder
                        .comment("Can attack while looking through Viewfinder.",
                                "Default: true")
                        .define("viewfinder_attacking", true);
                SELFIE_CAMERA_DISTANCE = builder
                        .comment("Camera distance in thirdperson-front mode. Default: 1.75")
                        .defineInRange("selfie_camera_distance", 1.75, 0.1, 4);
                WAIST_LEVEL_VIEWFINDER = builder
                        .comment("Shifts viewfinder view down to match waist-level camera position. Default: false.")
                        .define("waist_level_viewfinder", false);
                CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS = builder
                        .comment("Right-clicking a Camera in GUI will open Camera Attachments screen. Only in player inventory.",
                                "Default: true")
                        .define("right_click_attachments_screen", true);
                CAMERA_GUI_RIGHT_CLICK_HOTSWAP = builder
                        .comment("Right-clicking Camera in GUI with attachment item will insert/swap it.",
                                "Default: true")
                        .define("right_click_hotswap", true);
                TIMER_ATTRACTS_MOB_ATTENTION = builder
                        .comment("Self-timer will attract attention of nearby entities and makes them look at the Camera. Default: true")
                        .define("timer_attracts_mob_attention", true);
                TIMER_ATTENTION_RADIUS = builder
                        .comment("Radius in blocks around the camera in which mobs will be affected by the timer. Default: 16")
                        .defineInRange("timer_attention_radius", 16, 1, 64);
                builder.pop();
            }

            {
                builder.push("camera_stand");
                CAMERA_STAND_WORKING_RANGE = builder
                        .comment("Maximum allowed distance between Camera Stand and a player for the stand to function.",
                                "Camera on stand will capture the chunks that player is currently seeing, this may result is some parts of the world missing when camera on stand is taking photos while a player far away. Short render distances can be a problem as well.",
                                "Default: 100")
                        .defineInRange("working_range", 100, 1, Integer.MAX_VALUE);
                CAMERA_STAND_RANGE_MALFUNCTION = builder
                        .comment("Attempting to take a photo outside the working range will cause the Camera on stand to 'malfunction' and it would need to be repaired (by simply using it). Default: true")
                        .define("out_of_working_range_malfunction", true);
                CAMERA_STAND_FALLBACK_TO_OTHER_PLAYERS = builder
                        .comment("If owner of the Camera Stand is not in range, closest player will be chosen to create and render a photo. Default: true")
                        .define("fallback_to_other_players", true);
                builder.pop();
            }

            {
                builder.push("capture");
                DEFAULT_FRAME_SIZE = builder
                        .comment("Default size of an exposure image. High values take more disk space and can cause lag. Default: 320")
                        .defineInRange("default_frame_size", 320, 1, 2048);
                CAN_PROJECT = builder
                        .comment("Interplanar Projector can load images from URL or file on client's PC. Default: true")
                        .define("projecting_enabled", true);
                PROJECT_TIMEOUT_TICKS = builder
                        .comment("Time limit in ticks for projecting.",
                                "Default: 100 (5 seconds)")
                        .defineInRange("projecting_timeout_ticks", 100, 1, 200);
                builder.pop();
            }

            {
                builder.push("lightroom");
                LIGHTROOM_LIGHT_REQUIREMENT = builder
                        .comment("Light level that is required for Lightroom to work. Default: 13")
                        .defineInRange("lightroom_light_requirement", 13, 0, 15);
                builder.comment("Dyes that are used for particular printing process.",
                                "Valid dyes are: cyan, magenta, yellow, black.",
                                "Multiple definitions will make the lightroom consume multiple items per print. [\"black\", \"black\"] -> 2 Black Dye is consumed per print.");
                LIGHTROOM_BW_DYES = builder
                        .comment("Dyes for black and white print. Default: [\"black\"]")
                        .defineList("dyes_black_and_white", () -> List.of(DyeColor.BLACK.getName()), DyeColor.BLACK::getName, Server::validatePrintingDyes);
                LIGHTROOM_COLOR_DYES = builder
                        .comment("Dyes for color print. Default: [\"cyan\", \"magenta\", \"yellow\", \"black\"]")
                        .defineList("dyes_color", () -> List.of(DyeColor.CYAN.getName(), DyeColor.MAGENTA.getName(), DyeColor.YELLOW.getName(), DyeColor.BLACK.getName()), DyeColor.BLACK::getName, Server::validatePrintingDyes);
                LIGHTROOM_CHROMATIC_RED_DYES = builder
                        .comment("Dyes for chromatic red channel print. Default: [\"magenta\", \"yellow\"]")
                        .defineList("dyes_chromatic_red", () -> List.of(DyeColor.MAGENTA.getName(), DyeColor.YELLOW.getName()), DyeColor.BLACK::getName, Server::validatePrintingDyes);
                LIGHTROOM_CHROMATIC_GREEN_DYES = builder
                        .comment("Dyes for chromatic green channel print. Default: [\"cyan\", \"yellow\"]")
                        .defineList("dyes_chromatic_green", () -> List.of(DyeColor.CYAN.getName(), DyeColor.YELLOW.getName()), DyeColor.BLACK::getName, Server::validatePrintingDyes);
                LIGHTROOM_CHROMATIC_BLUE_DYES = builder
                        .comment("Dyes for chromatic blue channel print. Default: [\"cyan\", \"magenta\"]")
                        .defineList("dyes_chromatic_blue", () -> List.of(DyeColor.CYAN.getName(), DyeColor.MAGENTA.getName()), DyeColor.BLACK::getName, Server::validatePrintingDyes);
                LIGHTROOM_BW_PRINT_TIME = builder
                        .comment("Time in ticks to print black and white photograph. Default: 80")
                        .defineInRange("print_time_black_and_white", 80, 1, Integer.MAX_VALUE);
                LIGHTROOM_COLOR_PRINT_TIME = builder
                        .comment("Time in ticks to print color photograph. Default: 160")
                        .defineInRange("print_time_color", 160, 1, Integer.MAX_VALUE);
                LIGHTROOM_CHROMATIC_PRINT_TIME = builder
                        .comment("Time in ticks to print one channel of a chromatic photograph. Default: 120")
                        .defineInRange("print_time_chromatic", 120, 1, Integer.MAX_VALUE);
                LIGHTROOM_BW_EXPERIENCE = builder
                        .comment("Amount of experience awarded per printed black and white Photograph. Default: 2")
                        .defineInRange("experience_black_and_white", 2, 0, 99);
                LIGHTROOM_COLOR_EXPERIENCE = builder
                        .comment("Amount of experience awarded per printed color Photograph. Default: 4")
                        .defineInRange("experience_color", 4, 0, 99);
                LIGHTROOM_CHROMATIC_EXPERIENCE = builder
                        .comment("Amount of experience awarded per printed chromatic Photograph (when all three channels have been printed). Default: 5")
                        .defineInRange("experience_chromatic", 5, 0, 99);
                builder.pop();
            }

            {
                builder.push("photographs");
                STACKED_PHOTOGRAPHS_MAX_SIZE = builder
                        .comment("How many photographs can be stacked in Stacked Photographs item. Default: 16.",
                                "Larger numbers may cause errors. Use at your own risk. 32 should be fine though.")
                        .defineInRange("stacked_photographs_size", 16, 2, 64);
                builder.pop();
            }

            builder.push("misc");
            {
                FILM_ROLL_EASY_RENAMING = builder
                        .comment("Film rolls can be renamed by using the item. No experience cost. Default: true")
                        .define("film_roll_easy_renaming", true);
                INTERPLANAR_PROJECTOR_LARGER_RENAMING_LIMIT = builder
                        .comment("Increases item name length limit for Interplanar Projector to 150 characters. Vanilla limit: 50.",
                                "Default: true")
                        .define("increase_interplanar_projector_name_limit", true);
            }
            builder.pop();

            builder.comment("You wouldn't need to touch these settings most likely. They are there to help debug/fix some weird issues.")
                    .push("debug");
            {
                CLEANUP_TIMED_OUT_EXPECTED_EXPOSURES = builder
                        .comment("Clean up data about timed-out expected exposure uploads on level/server save. Default: true")
                        .define("cleanup_timed_out_expected_uploads", true);
            }
            builder.pop();

            SPEC = builder.build();
        }

        private static boolean validatePrintingDyes(Object obj) {
            List<String> validColors = List.of(DyeColor.CYAN.getName(), DyeColor.MAGENTA.getName(), DyeColor.YELLOW.getName(), DyeColor.BLACK.getName());
            if (!(obj instanceof String color) || !validColors.contains(color)) {
                Exposure.LOGGER.error("Invalid dye color for printing: {} is not allowed. Allowed: Cyan, Magenta, Yellow and Black.", obj);
                return false;
            }
            return true;
        }
    }

    public static class Common {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.BooleanValue SIGNED_ALBUM_GLINT;
        public static final ModConfigSpec.BooleanValue DIFFERENT_DEVELOPING_POTION_COLORS;
        public static final ModConfigSpec.BooleanValue GENERATE_LOOT;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            builder.push("misc");
            {
                SIGNED_ALBUM_GLINT = builder
                        .comment("Signed Album item will have an enchantment glint.")
                        .define("signed_album_glint", true);

                DIFFERENT_DEVELOPING_POTION_COLORS = builder
                        .comment("Mundane, Awkward and Thick potions will have their color changed slightly, so it's easier to tell them apart. Default: true")
                        .define("different_developing_potions_colors", true);

                GENERATE_LOOT = builder
                        .comment("Generate photographs and film rolls in loot chests. Default: true")
                        .define("loot_chests", true);
            }
            builder.pop();

            SPEC = builder.build();
        }
    }

    public static class Client {
        public static final ModConfigSpec SPEC;

        // UI
        public static final ModConfigSpec.BooleanValue RECIPE_TOOLTIPS_WITHOUT_JEI;
        public static final ModConfigSpec.BooleanValue CAMERA_SHOW_TOOLTIP_DETAILS;
        public static final ModConfigSpec.BooleanValue CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP;
        public static final ModConfigSpec.BooleanValue CAMERA_SHOW_FILM_BAR_ON_ITEM;
        public static final ModConfigSpec.BooleanValue PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP;
        public static final ModConfigSpec.BooleanValue PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR;
        public static final ModConfigSpec.BooleanValue CAMERA_STAND_TOOLTIP;

        public static final ModConfigSpec.BooleanValue ALBUM_PHOTOS_COUNT_TOOLTIP;
        public static final ModConfigSpec.ConfigValue<String> ALBUM_FONT_MAIN_COLOR;
        public static final ModConfigSpec.ConfigValue<String> ALBUM_FONT_SECONDARY_COLOR;
        public static final ModConfigSpec.ConfigValue<String> ALBUM_SELECTION_COLOR;
        public static final ModConfigSpec.ConfigValue<String> ALBUM_SELECTION_UNFOCUSED_COLOR;

        // VIEWFINDER
        public static final ModConfigSpec.BooleanValue VIEWFINDER_MIDDLE_CLICK_CONTROLS;
        public static final ModConfigSpec.DoubleValue VIEWFINDER_ZOOM_SENSITIVITY_INFLUENCE;
        public static final ModConfigSpec.ConfigValue<String> VIEWFINDER_BACKGROUND_COLOR;
        public static final ModConfigSpec.ConfigValue<String> VIEWFINDER_FONT_MAIN_COLOR;
        public static final ModConfigSpec.ConfigValue<String> VIEWFINDER_FONT_SECONDARY_COLOR;

        // CAPTURE
        public static final ModConfigSpec.BooleanValue KEEP_POST_EFFECT;
        public static final ModConfigSpec.IntValue FLASH_CAPTURE_DELAY_TICKS;
        public static final ModConfigSpec.BooleanValue FORCE_DIRECT_CAPTURE;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> FORCE_DIRECT_CAPTURE_MODS;
        public static final ModConfigSpec.IntValue DIRECT_CAPTURE_DELAY_FRAMES;

        // RENDER
        public static final ModConfigSpec.BooleanValue PIXEL_PERFECT_PHOTOGRAPH_FRAME;
        public static final ModConfigSpec.BooleanValue PHOTOGRAPH_RENDERS_IN_ITEM_FRAME;
        public static final ModConfigSpec.BooleanValue HIDE_PROJECTED_PHOTOGRAPHS_MADE_BY_OTHERS;
        public static final ModConfigSpec.BooleanValue HIDE_ALL_PHOTOGRAPHS_MADE_BY_OTHERS;
        public static final ModConfigSpec.IntValue PHOTOGRAPH_FRAME_CULLING_DISTANCE;
        public static final ModConfigSpec.DoubleValue PHOTOGRAPH_FRAME_IMAGE_OFFSET;

        // INTEGRATION
        public static final ModConfigSpec.BooleanValue SHOW_JEI_INFORMATION;
        public static final ModConfigSpec.BooleanValue REAL_CAMERA_DISABLE_IN_VIEWFINDER;

        // IMAGE SAVING
        public static final ModConfigSpec.BooleanValue EXPORT_PHOTOGRAPH_WHEN_VIEWED;
        public static final ModConfigSpec.BooleanValue EXPORT_ORGANIZE_BY_WORLD;
        public static final ModConfigSpec.IntValue EXPORT_SIZE_MULTIPLIER;

        // MISC
        public static final ModConfigSpec.BooleanValue ATTACHMENTS_SHOW_INFO_TOAST;
        public static final ModConfigSpec.BooleanValue ATTACHMENTS_SHOW_WIKI_TOAST;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            {
                builder.push("ui");

                RECIPE_TOOLTIPS_WITHOUT_JEI = builder
                        .comment("Tooltips for Developing Film Rolls and Copying Photographs will be shown on Film Rolls and Photographs respectively, describing the crafting recipe. ",
                                "Only when JEI is not installed. (Only JEI shows these recipes, not REI or EMI)")
                        .define("recipe_tooltips_without_jei", true);

                CAMERA_SHOW_TOOLTIP_DETAILS = builder
                        .comment("Details about Camera configuring will be shown in Camera item tooltip.")
                        .define("camera_details_tooltip", true);

                CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP = builder
                        .comment("Film Roll Frames will be shown in the camera tooltip.",
                                "Default: true")
                        .define("camera_film_frames_tooltip", true);

                CAMERA_SHOW_FILM_BAR_ON_ITEM = builder
                        .comment("Film Roll fullness bar will be shown on the Camera item.",
                                "Default: false")
                        .define("camera_shows_film_bar", false);

                PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP = builder
                        .comment("Photographer name will be shown in Photograph's tooltip.")
                        .define("photograph_photographer_name_tooltip", false);

                PHOTOGRAPH_IN_HAND_HIDE_CROSSHAIR = builder
                        .comment("Crosshair will not get in the way when holding a photograph.")
                        .define("photograph_in_hand_hide_crosshair", true);

                CAMERA_STAND_TOOLTIP = builder
                        .comment("When looking at the Camera Stand, in-world tooltip will show information about the camera on it. Default: true")
                        .define("camera_stand_tooltip", true);

                ALBUM_PHOTOS_COUNT_TOOLTIP = builder
                        .comment("Album will show how many photographs it contains in a tooltip.")
                        .define("album_show_photos_count", true);

                {
                    builder.push("album");
                    ALBUM_FONT_MAIN_COLOR = builder
                            .comment("Color in hex format. AARRGGBB.").define("font_main_color", "FFB59774");
                    ALBUM_FONT_SECONDARY_COLOR = builder
                            .comment("Color in hex format. AARRGGBB.").define("font_secondary_color", "FFEFE4CA");
                    ALBUM_SELECTION_COLOR = builder
                            .comment("Color in hex format. AARRGGBB.").define("selection_color", "FF8888FF");
                    ALBUM_SELECTION_UNFOCUSED_COLOR = builder
                            .comment("Color in hex format. AARRGGBB.").define("selection_unfocused_color", "FFBBBBFF");
                    builder.pop();
                }

                builder.pop();
            }

            {
                builder.push("viewfinder");
                VIEWFINDER_MIDDLE_CLICK_CONTROLS = builder
                        .comment("Clicking middle mouse button will open Viewfinder Controls. This is independent of Open Camera Controls keybind.",
                                "Allows opening camera controls without dismounting from a vehicle - and keeping controls on sneak or other button as well.",
                                "Default: true")
                        .define("middle_click_controls", true);
                VIEWFINDER_ZOOM_SENSITIVITY_INFLUENCE = builder
                        .comment("How much zooming influences mouse sensitivity.",
                                "0 - no change to sensitivity. 1 - full effect.",
                                "Default: 0.75")
                        .defineInRange("zoom_sensitivity_influence", 0.75, 0.0, 1.0);
                VIEWFINDER_BACKGROUND_COLOR = builder
                        .comment("Color in hex format. AARRGGBB.").define("background_color", "FA1F1D1B");
                VIEWFINDER_FONT_MAIN_COLOR = builder
                        .comment("Color in hex format. AARRGGBB.").define("font_main_color", "FF2B2622");
                VIEWFINDER_FONT_SECONDARY_COLOR = builder
                        .comment("Color in hex format. AARRGGBB.").define("font_secondary_color", "FF7A736C");
                builder.pop();
            }

            {
                builder.push("capture");
                KEEP_POST_EFFECT = builder
                        .comment("Keep Post Effect (vanilla shader) when capturing an image.",
                                "It is sometimes used by mods to change how player sees the world. (Cold Sweat's overheating blur, Supplementaries mob heads, for example).",
                                "In vanilla, it's only used when spectating a creeper/enderman/etc.",
                                "Default: false")
                        .define("keep_post_effect", false);
                FLASH_CAPTURE_DELAY_TICKS = builder
                        .comment("Delay in ticks before capturing an image when shooting with flash." +
                                "\nIf you experience flash synchronization issues (Flash having no effect on the image) - try increasing the value.")
                        .defineInRange("flash_capture_delay_ticks", 4, 1, FlashBlock.LIFETIME_TICKS);
                FORCE_DIRECT_CAPTURE = builder
                        .comment("Force legacy (pre 1.21) capturing method for taking images. Enable if you experiencing issues with resulting images.",
                                "Direct method will be used regardless of this setting if mods defined in 'mods_needing_direct_capture' is installed.",
                                "Default: false")
                        .define("force_direct_capture", false);
                FORCE_DIRECT_CAPTURE_MODS = builder
                        .comment("Direct capture will be used if any of these mods is installed.",
                                "Format: '[\"mod_id\", \"mod_id\"]'. Default: [" + String.join(", ", Exposure.MODS_REQUIRING_DIRECT_CAPTURE) + "]")
                        .defineList("force_direct_capture_mods", () -> Exposure.MODS_REQUIRING_DIRECT_CAPTURE, () -> "mod_id", o -> true);
                DIRECT_CAPTURE_DELAY_FRAMES = builder
                        .comment("Delay in frames before capturing an image if 'direct_capture' method is in use.",
                                "Set to higher value when leftovers of GUI elements (such as nameplates) are visible on the images",
                                "(some shaders have temporal effects that take several frames to disappear fully)")
                        .defineInRange("direct_capture_delay_frames", 0, 0, 100);
                builder.pop();
            }

            {
                builder.push("render");
                PIXEL_PERFECT_PHOTOGRAPH_FRAME = builder
                        .comment("Makes photos in Photograph Frame render with alignment to 16 pixel grid (like paintings). Just for fun. Default: false")
                        .define("pixel_perfect_photograph_frame", false);
                PHOTOGRAPH_RENDERS_IN_ITEM_FRAME = builder
                        .comment("Photographs in Item Frame will be rendered as image instead of an item icon. Default: false")
                        .define("photograph_renders_in_item_frame", false);
                HIDE_PROJECTED_PHOTOGRAPHS_MADE_BY_OTHERS = builder
                        .comment("Projected photographs (using Interplanar Projector) made by other players will be censored (pixelated). Default: false")
                        .define("censor_projected_photographs_made_by_others", false);
                HIDE_ALL_PHOTOGRAPHS_MADE_BY_OTHERS = builder
                        .comment("All photographs made by other players will will be censored (pixelated). Default: false")
                        .define("censor_all_photographs_made_by_others", false);
                PHOTOGRAPH_FRAME_CULLING_DISTANCE = builder
                        .comment("Distance from the player beyond which Photograph Frame would not be rendered. Default: 64",
                                "Note: this number may not relate to distance in blocks exactly. It's influenced by render distance and entity distance settings.")
                        .defineInRange("photograph_frame_culling_distance", 64, 8, 128);
                PHOTOGRAPH_FRAME_IMAGE_OFFSET = builder
                        .comment("Depth offset to Photograph in Photograph Frame. Can be used to fix issues with some 3D resourcepacks. Value of 0.015 is good for 'Classic 3D' resourcepack. Default: 0.0")
                        .defineInRange("photograph_frame_image_offset", 0.0, -1.0, 1.0);
                builder.pop();
            }

            {
                builder.push("integration");
                SHOW_JEI_INFORMATION = builder
                        .comment("Useful information about some items will be shown in JEI description category. Default: true")
                        .define("jei_information", true);
                REAL_CAMERA_DISABLE_IN_VIEWFINDER = builder
                        .comment("[Real Camera] Disable player model rendering when looking through viewfinder. Default: true")
                        .define("real_camera_disable_in_viewfinder", true);
                builder.pop();
            }

            {
                builder.push("export");
                EXPORT_PHOTOGRAPH_WHEN_VIEWED = builder
                        .comment("When the Photograph you took is viewed in UI (by using a Photograph), image will be exported to '<instance>/exposures' folder as a png.")
                        .define("export_viewed_photographs", true);
                EXPORT_ORGANIZE_BY_WORLD = builder
                        .comment("When exporting, exposures will be organized to subfolders with world name.")
                        .define("export_organize_by_world", true);
                EXPORT_SIZE_MULTIPLIER = builder
                        .comment("Exported exposures will be scaled by this multiplier.",
                                "Given the default exposure size of 320 pixels - this will produce:",
                                "320/640/960/1280/etc image. Be careful with larger frame sizes.",
                                "Default: 2")
                        .defineInRange("export_size_multiplier", 2, 1, 10);

                builder.pop();
            }

            {
                builder.push("tutorial");
                ATTACHMENTS_SHOW_INFO_TOAST = builder
                        .comment("Toast that teaches hovering mouse over camera parts will be shown when attachments menu is first opened. Default: true.",
                                "*This setting will be automatically set to false on first show.*")
                        .define("attachments_show_info_toast", true);
                ATTACHMENTS_SHOW_WIKI_TOAST = builder
                        .comment("Toast that teaches wiki opening will be shown when attachments menu is first opened, after info toast. Default: true.",
                                "*This setting will be automatically set to false on first show.*")
                        .define("attachments_show_wiki_toast", true);
                builder.pop();
            }

            SPEC = builder.build();
        }
    }

    public static int getColor(ModConfigSpec.ConfigValue<String> config) {
        String value = config.get();
        try {
            return Color.fromHex(value).getARGB();
        } catch (Exception e) {
            Exposure.LOGGER.error("{} is not valid ARGB color. {}", value, String.join("/", config.getPath()));
            return 0;
        }
    }
}