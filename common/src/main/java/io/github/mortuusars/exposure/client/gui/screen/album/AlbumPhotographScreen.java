package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlbumPhotographScreen extends PhotographScreen {
    private final AlbumScreen screen;

    public AlbumPhotographScreen(AlbumScreen screen, List<ItemAndStack<PhotographItem>> photographs) {
        super(photographs);
        this.screen = screen;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (zoom.targetZoom == zoom.minZoom) {
            close();
            return;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE || minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            zoom.set(0f);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (handled)
            return true;

        if (button == 1) { // Right Click
            zoom.set(0f);
            return true;
        }

        return false;
    }

    public void close() {
        Minecraft.getInstance().setScreen(screen);
        if (minecraft.player != null)
            minecraft.player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, 0.7f);
    }
}
