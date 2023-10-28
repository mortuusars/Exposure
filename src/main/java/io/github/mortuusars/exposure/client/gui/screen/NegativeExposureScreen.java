package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.client.gui.component.ZoomableScreen;
import io.github.mortuusars.exposure.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class NegativeExposureScreen extends ZoomableScreen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;

    private final String exposureId;

    public NegativeExposureScreen(String exposureId) {
        super(Component.empty());
        this.exposureId = exposureId;

        zoom.step = 2f;
        zoom.defaultZoom = 1f;
        zoom.targetZoom = 1f;
        zoom.minZoom = zoom.defaultZoom / (float)Math.pow(zoom.step, 3f);
        zoom.maxZoom = zoom.defaultZoom * (float)Math.pow(zoom.step, 3f);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        zoomFactor = 1f / minecraft.options.guiScale().get();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        super.render(poseStack, mouseX, mouseY, partialTick);

        Exposure.getStorage().getOrQuery(exposureId).ifPresent(exposureData -> {
            int width = exposureData.getWidth();
            int height = exposureData.getHeight();
            boolean colorFilm = exposureData.getType() == FilmType.COLOR;

            poseStack.pushPose();
            poseStack.translate(Math.round(x + this.width / 2f), Math.round(y + this.height / 2f), 0);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-Math.round(width / 2f), -Math.round(height / 2f), 0);

            {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderTexture(0, TEXTURE);

                poseStack.pushPose();
                float scale = Math.max((float) width / (FRAME_SIZE), (float) height / (FRAME_SIZE));
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-12, -12, 0);

                GuiUtil.blit(poseStack, 0, 0, BG_SIZE, BG_SIZE, 0, 0, 256, 256, 0);
                if (colorFilm)
                    RenderSystem.setShaderColor(1.2F, 0.96F, 0.75F, 1.0F);
                GuiUtil.blit(poseStack, 0, 0, BG_SIZE, BG_SIZE, 0, BG_SIZE, 256, 256, 0);

                poseStack.popPose();
            }

            if (colorFilm)
                ExposureClient.getExposureRenderer().renderNegative(exposureId, exposureData, true, poseStack,
                        width, height, LightTexture.FULL_BRIGHT, 180, 130, 110, 255);
            else
                ExposureClient.getExposureRenderer().renderNegative(exposureId, exposureData, true, poseStack,
                        width, height, LightTexture.FULL_BRIGHT, 255, 255, 255, 255);

            poseStack.popPose();
        });
    }
}
