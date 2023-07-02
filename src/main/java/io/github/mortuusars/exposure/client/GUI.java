package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.camera.Photograph;
import io.github.mortuusars.exposure.client.screen.ExposureScreen;
import io.github.mortuusars.exposure.client.screen.PhotographScreen;
import io.github.mortuusars.exposure.client.viewfinder.ViewfinderControlsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class GUI {
    public static void showExposureViewScreen(ItemStack film) {
        Minecraft.getInstance().setScreen(new ExposureScreen(film));
    }

    public static void showPhotographScreen(Photograph photograph) {
        Minecraft.getInstance().setScreen(new PhotographScreen(photograph));
    }

    public static void showViewfinderConfigScreen() {
        Minecraft.getInstance().setScreen(new ViewfinderControlsScreen());
    }
}
