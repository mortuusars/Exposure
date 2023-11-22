package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.camera.infrastructure.ZoomDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public abstract class ZoomableScreen extends Screen {
    protected final ZoomHandler zoom = new ZoomHandler();
    protected float zoomFactor = 1f;
    protected float scale = 1f;
    protected float x;
    protected float y;

    @NotNull
    protected final Minecraft minecraft = Minecraft.getInstance();

    protected ZoomableScreen(Component title) {
        super(title);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        zoom.update(partialTick);
        scale = zoom.get() * zoomFactor;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled)
            return true;

        if (minecraft.options.keyInventory.matches(keyCode, scanCode))
            this.onClose();
        else if (keyCode == InputConstants.KEY_ADD || keyCode == InputConstants.KEY_EQUALS)
            zoom.change(ZoomDirection.IN);
        else if (keyCode == 333 /*KEY_SUBTRACT*/ || keyCode == InputConstants.KEY_MINUS)
            zoom.change(ZoomDirection.OUT);
        else
            return false;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean handled = super.mouseScrolled(mouseX, mouseY, delta);

        if (!handled) {
            zoom.change(delta >= 0.0 ? ZoomDirection.IN : ZoomDirection.OUT);
            return true;
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean handled = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        if (!handled && button == 0) { // Left Click
            float centerX = width / 2f;
            float centerY = height / 2f;

            x = (float) Mth.clamp(x + dragX, -centerX, centerX);
            y = (float) Mth.clamp(y + dragY, -centerY, centerY);
            handled = true;
        }

        return handled;
    }
}
