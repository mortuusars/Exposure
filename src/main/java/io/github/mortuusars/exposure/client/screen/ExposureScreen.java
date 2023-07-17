package io.github.mortuusars.exposure.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.Exposure;
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
    private List<ExposureFrame> exposureFrames = new ArrayList<>();
    private int currentExposureIndex;
    private int frameSize;

    public ExposureScreen(ItemStack film) {
        super(Component.empty());
        minecraft = Minecraft.getInstance();

        // TODO: remove?
        ExposureRenderer.clearData();

        if (!(film.getItem() instanceof FilmItem filmItem) || filmItem.getExposedFrames(film).size() == 0) {
            currentExposureIndex = -1;
            this.onClose();
        }
        else {
            List<ExposureFrame> frames = filmItem.getExposedFrames(film);
            exposureFrames = frames.stream().filter(frame -> !StringUtil.isNullOrEmpty(frame.id)).collect(Collectors.toList());
            frameSize = filmItem.getFrameSize(film);

            currentExposureIndex = exposureFrames.size() - 1;
            loadExposure();

            Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
        }
    }

    @Override
    protected String getExposureId() {
        return currentExposureIndex >= 0 ? exposureFrames.get(currentExposureIndex).id : "";
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(poseStack);
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);

        float scale = (height - (height / 6f)) / frameSize;
        poseStack.pushPose();

        // Move to center
        poseStack.translate(width / 2f, height / 2f, 0);
        // Scale
        poseStack.scale(scale, scale, scale);
        // Set origin point to center (for scale)
        poseStack.translate(frameSize / -2d, frameSize / -2d, 0);


        if (exposureData != null) {
            fill(poseStack, -8, -8, frameSize + 8, frameSize + 8, 0xFFDDDDDD);
            renderExposure(poseStack, false);
        }
        else {
            loadExposure();
            RenderSystem.setShaderTexture(0, Exposure.resource("textures/misc/missing_image.png"));
            RenderSystem.setShaderColor(1, 1, 1, 1);
            blit(poseStack, 0, 0, getBlitOffset(), 0, 0, frameSize, frameSize, frameSize, frameSize);
        }

        poseStack.popPose();
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_LEFT) {
            currentExposureIndex = (Math.max(0, currentExposureIndex - 1));
            loadExposure();
        }
        else if (key == InputConstants.KEY_RIGHT) {
            currentExposureIndex = (Math.min(exposureFrames.size() - 1, currentExposureIndex + 1));
            loadExposure();
        }
        else if (key == InputConstants.KEY_P) { //TODO: Proper printing
            ExposureFrame exposureFrame = exposureFrames.get(currentExposureIndex);
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
