package io.github.mortuusars.exposure.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class GUI {
    public static void showExposureViewScreen(ItemStack film) {
        Minecraft.getInstance().setScreen(new ExposureScreen(film));
    }
}
