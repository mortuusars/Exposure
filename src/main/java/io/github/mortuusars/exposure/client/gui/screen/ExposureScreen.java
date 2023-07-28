package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.client.render.PhotographRenderer;
import io.github.mortuusars.exposure.item.FilmRollItem;
import io.github.mortuusars.exposure.item.IFilmItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ExposureScreen extends Screen {
    private List<ExposureFrame> exposureFrames = new ArrayList<>();
    private int currentExposureIndex = -1;

    public ExposureScreen(ItemStack film) {
        super(Component.empty());
        minecraft = Minecraft.getInstance();

        if (!(film.getItem() instanceof IFilmItem filmItem)) {
            this.onClose();
            return;
        }

        List<ExposureFrame> frames = filmItem.getExposedFrames(film)
                .stream()
                .filter(frame -> !StringUtil.isNullOrEmpty(frame.id))
                .toList();

        if (frames.size() == 0) {
            this.onClose();
            return;
        }

        exposureFrames = frames;
        currentExposureIndex = 0;

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    protected String getExposureId() {
        return exposureFrames.size() > 0 && currentExposureIndex >= 0 ? exposureFrames.get(currentExposureIndex).id : "";
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(poseStack);
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);

        String exposureId = getExposureId();

        if (exposureId.length() == 0)
            return;

        float scale = (height - (height / 6f)) / PhotographRenderer.SIZE;
        poseStack.pushPose();

        // Move to center
        poseStack.translate(width / 2f, height / 2f, 0);
        // Scale
        poseStack.scale(scale, scale, scale);
        // Set origin point to center (for scale)
        poseStack.translate(PhotographRenderer.SIZE / -2d, PhotographRenderer.SIZE / -2d, 0);

        fill(poseStack, -8, -8, PhotographRenderer.SIZE + 8, PhotographRenderer.SIZE + 8, 0xFFDDDDDD);
        Exposure.getStorage().getOrQuery(exposureId).ifPresent(exposureData -> {
            ExposureClient.getExposureRenderer().render(exposureId, exposureData, false, poseStack,
                    LightTexture.FULL_BRIGHT, PhotographRenderer.SIZE, PhotographRenderer.SIZE);
        });

        poseStack.popPose();
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_LEFT)
            currentExposureIndex = (Math.max(0, currentExposureIndex - 1));
        else if (key == InputConstants.KEY_RIGHT)
            currentExposureIndex = (Math.min(exposureFrames.size() - 1, currentExposureIndex + 1));
        else if (Minecraft.getInstance().options.keyInventory.matches(key, scanCode))
            this.onClose();
        else
            return super.keyPressed(key, scanCode, modifiers);

        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
