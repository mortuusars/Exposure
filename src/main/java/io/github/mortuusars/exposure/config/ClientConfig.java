package io.github.mortuusars.exposure.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.awt.*;

public class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_FONT_MAIN_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> VIEWFINDER_FONT_SECONDARY_COLOR;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("ViewfinderUI");
        VIEWFINDER_FONT_MAIN_COLOR = builder.define("FontMainColorHex", "FF5A5552");
        VIEWFINDER_FONT_SECONDARY_COLOR = builder.define("FontSecondaryColorHex", "FFB7AFAB");
        builder.pop();

        SPEC = builder.build();
    }

    public static int getMainFontColor() {
        return new Color((int)Long.parseLong(VIEWFINDER_FONT_MAIN_COLOR.get(), 16), true).getRGB();
    }

    public static int getSecondaryFontColor() {
        return new Color((int)Long.parseLong(VIEWFINDER_FONT_SECONDARY_COLOR.get(), 16), true).getRGB();
    }
}
