package io.github.mortuusars.exposure.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.camera.Photograph;
import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import io.github.mortuusars.exposure.client.screen.base.ExposureRenderScreen;
import io.github.mortuusars.exposure.item.FilmItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ServerboundPrintPhotographPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExposureScreen extends ExposureRenderScreen {
    private List<ExposureFrame> exposureIds = new ArrayList<>();
    private int currentExposureIndex;
    private int frameSize;

    public ExposureScreen(ItemStack film) {
        super(Component.empty());

        // TODO: remove?
        ExposureRenderer.clearData();

        if (!(film.getItem() instanceof FilmItem filmItem)) {
            this.onClose();
            return;
        }

        List<ExposureFrame> frames = filmItem.getExposedFrames(film);
        exposureIds = frames.stream().filter(frame -> !StringUtil.isNullOrEmpty(frame.id)).collect(Collectors.toList());
        frameSize = filmItem.getFrameSize(film);

        currentExposureIndex = exposureIds.size() - 1;
        loadExposure();

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    protected String getExposureId() {
        return exposureIds.get(currentExposureIndex).id;
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

            fill(poseStack, -8, -8, exposureData.getWidth() + 8, exposureData.getHeight() + 8, 0xFFDDDDDD);
            renderExposure(poseStack, false);
            poseStack.popPose();

            bufferSource.endBatch();
        }
        else {
            loadExposure();
        }
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_LEFT) {
            currentExposureIndex = (Math.max(0, currentExposureIndex - 1));
            loadExposure();
        }
        else if (key == InputConstants.KEY_RIGHT) {
            currentExposureIndex = (Math.min(exposureIds.size() - 1, currentExposureIndex + 1));
            loadExposure();
        }
        else if (key == InputConstants.KEY_P) { //TODO: Proper printing
            ExposureFrame exposureFrame = exposureIds.get(currentExposureIndex);
            Packets.sendToServer(new ServerboundPrintPhotographPacket(new Photograph(exposureFrame.id, frameSize, "")));
        }
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
