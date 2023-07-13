package io.github.mortuusars.exposure.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.awt.*;

public class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_FONT_MAIN_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_FONT_SECONDARY_COLOR;

    public static final ForgeConfigSpec.ConfigValue<String> EXPOSURE_SAVE_PATH;
    public static final ForgeConfigSpec.BooleanValue EXPOSURE_SAVE_LEVEL_SUBFOLDER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("ViewfinderUI");
        VIEWFINDER_FONT_MAIN_COLOR = builder.define("FontMainColorHex", "FF5A5552");
        VIEWFINDER_FONT_SECONDARY_COLOR = builder.define("FontSecondaryColorHex", "FFB7AFAB");
        builder.pop();

        builder.push("Saving");
        EXPOSURE_SAVE_PATH = builder.define("ExposureSavePath", "exposures");
        EXPOSURE_SAVE_LEVEL_SUBFOLDER = builder.define("ExposureSavePutInLevelSubfolder", true);
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
