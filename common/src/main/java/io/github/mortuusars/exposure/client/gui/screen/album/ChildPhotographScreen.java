package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChildPhotographScreen extends PhotographScreen {
    private final Screen parentScreen;

    public ChildPhotographScreen(Screen parentScreen, List<ItemAndStack<PhotographItem>> photographs) {
        super(photographs);
        this.parentScreen = parentScreen;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (zoom.get() < zoom.getMin() + 0.1f && zoom.getTarget() < zoom.getMin() + 0.1f) {
            Minecrft.get().setScreen(parentScreen);
            Minecrft.player().playSound(Exposure.SoundEvents.PHOTOGRAPH_PLACE.get(), 0.7f, 1.1f);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE || Minecrft.options().keyInventory.matches(event)) {
            onClose();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean handled) {
        if (super.mouseClicked(event, handled)) {
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            zoom.setTarget(0f);
            return true;
        }

        return false;
    }

    @Override
    public void onClose() {
        zoom.setTarget(0f); // ChildPhotographScreen#render will close screen when zooming out ends.
    }
}
