package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class LightroomFrameInspectScreen extends FilmFrameInspectScreen {
    private final LightroomScreen lightroomScreen;

    public LightroomFrameInspectScreen(LightroomScreen lightroomScreen) {
        super(lightroomScreen.getMenu().getExposedFrames(), lightroomScreen.getMenu().getSelectedFrame());
        this.lightroomScreen = lightroomScreen;
        this.pager.setChangeSound(null);
    }

    @Override
    protected void pageChanged(int oldPage, int newPage) {
        // Lightroom can only change one at a time.
        // It should always be one anyway, but limiting it just in case would do no harm.
        PagingDirection direction = PagingDirection.fromChange(oldPage, newPage);
        Collections.rotate(frames, -direction.getValue());

        if (newPage != lightroomScreen.getMenu().getSelectedFrame()) {
            lightroomScreen.changeFrame(direction);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (zoom.get() < zoom.getMin() + 0.1f && zoom.getTarget() < zoom.getMin() + 0.1f) {
            Minecrft.get().setScreen(lightroomScreen);
            Minecrft.playerOrThrow().playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, 0.7f);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            zoom.setTarget(0f);
            return true;
        }

        return false;
    }

    @Override
    public void onClose() {
        zoom.setTarget(0f); // LightroomFrameInspectScreen#render will close screen when zooming out ends.
    }
}
