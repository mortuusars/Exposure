package io.github.mortuusars.exposure.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import io.github.mortuusars.exposure.item.FilmItem;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExposureScreen extends Screen {
    @Nullable
    private ExposureSavedData exposureData;

    private List<ExposureFrame> exposureIds = new ArrayList<>();
    private int currentExposureIndex;

    public ExposureScreen(ItemStack film) {
        super(Component.empty());

        if (film.getItem() instanceof FilmItem filmItem) {
            List<ExposureFrame> frames = filmItem.getFrames(film);
            exposureIds = frames.stream().filter(frame -> !StringUtil.isNullOrEmpty(frame.id)).collect(Collectors.toList());
        }

        currentExposureIndex = exposureIds.size() - 1;
        loadExposure();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(poseStack);
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);

//        fill(poseStack, 0, 0, width, height, 0x90000000);

        if (exposureData != null) {

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            float scale = (height - (height / 6f)) / exposureData.getHeight();

//            float scale = 1f / (exposureData.getHeight() / ((float)height));
            float x = (width - exposureData.getWidth()) / 2f / scale;
            float y = (height - exposureData.getHeight()) / 2f / scale;

            poseStack.pushPose();

            // Move to center
            poseStack.translate(width / 2f, height / 2f, 0);
            // Scale
            poseStack.scale(scale, scale, scale);
            // Set origin point to center (for scale)
            poseStack.translate(exposureData.getWidth() / -2d, exposureData.getHeight() / -2d, 0);
//            poseStack.translate(x, y, 0);

            fill(poseStack, -8, -8, exposureData.getWidth() + 8, exposureData.getHeight() + 8, 0xFFDDDDDD);
            ExposureRenderer.render(poseStack, bufferSource, exposureIds.get(currentExposureIndex).id + "asd", exposureData, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
    //        float scale = 2f / mps.length;
    //        int startX = (int) (centerX - (mps.length * 128 * scale) / 2);
    //        int startY = (int) (centerY - (mps.length * 128 * scale) / 2);
    //
    //        for (int column = 0; column < mps.length; column++) {
    //            for (int row = 0; row < mps[0].length; row++) {
    //                poseStack.pushPose();
    //                poseStack.translate(startX + (column * 128 * scale), startY + (row * 128 * scale), 0);
    //                poseStack.scale(scale, scale, scale);
    //                Minecraft.getInstance().gameRenderer.getMapRenderer().render(poseStack, bufferSource,
    //                        row + column * 4, mps[column][row], true, LightTexture.FULL_BRIGHT);
    //                poseStack.popPose();
    //            }
    //        }

            bufferSource.endBatch();
        }
        else {
            loadExposure();
        }
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_LEFT)
            currentExposureIndex = (Math.max(0, currentExposureIndex - 1));
        else if (key == InputConstants.KEY_RIGHT)
            currentExposureIndex = (Math.min(exposureIds.size() - 1, currentExposureIndex + 1));

        loadExposure();

        return super.keyPressed(key, scanCode, modifiers);
    }

    private void loadExposure() {
        exposureData = ExposureStorage.CLIENT.getOrQuery(exposureIds.get(currentExposureIndex).id).orElse(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
