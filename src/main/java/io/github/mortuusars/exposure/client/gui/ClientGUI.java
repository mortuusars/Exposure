package io.github.mortuusars.exposure.client.gui;

import io.github.mortuusars.exposure.client.gui.screen.DevelopedFilmScreen;
import io.github.mortuusars.exposure.client.gui.screen.ExposureScreen;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.client.gui.screen.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ClientGUI {
    public static void openExposureViewScreen(ItemStack film) {
        Minecraft.getInstance().setScreen(new ExposureScreen(film));
    }

    public static void openPhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
        Minecraft.getInstance().setScreen(new PhotographScreen(photographs));
    }

    public static void openViewfinderControlsScreen() {
        Minecraft.getInstance().setScreen(new ViewfinderControlsScreen());
    }

    public static void openDevelopedFilmScreen(ItemAndStack<DevelopedFilmItem> film) {
        Minecraft.getInstance().setScreen(new DevelopedFilmScreen(film));
    }
}
