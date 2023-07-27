package io.github.mortuusars.exposure.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.awt.*;

public class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    // VIEWFINDER
    public static final ForgeConfigSpec.DoubleValue VIEWFINDER_CROP_FACTOR;
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
        VIEWFINDER_FONT_MAIN_COLOR = builder.define("FontMainColorHex", "FF5A5552");
        VIEWFINDER_FONT_SECONDARY_COLOR = builder.define("FontSecondaryColorHex", "FFB7AFAB");
        builder.pop();

        builder.push("ExposureFileSaving");
        EXPOSURE_SAVE_PATH = builder.define("FolderPath", "exposures");
        EXPOSURE_SAVE_LEVEL_SUBFOLDER = builder.define("PutInLevelSubfolder", true);
        EXPOSURE_SAVE_ON_EVERY_CAPTURE = builder.define("SaveOnEveryCapture", true); //TODO: remove before release
        builder.pop();

        SPEC = builder.build();
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
