package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.client.screen.ExposureScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class GUI {
    public static void showExposureViewScreen(ItemStack film) {
        Minecraft.getInstance().setScreen(new ExposureScreen(film));
    }
}
