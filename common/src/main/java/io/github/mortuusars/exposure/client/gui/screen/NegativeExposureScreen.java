package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.client.render.ExposureImage;
import io.github.mortuusars.exposure.client.render.ExposureTexture;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure.util.GuiUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NegativeExposureScreen extends ZoomableScreen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;

    private final Either<String, ResourceLocation> idOrTexture;

    public NegativeExposureScreen(@NotNull Either<String, ResourceLocation> idOrTexture) {
        super(Component.empty());
        this.idOrTexture = idOrTexture;

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
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        FilmType type = idOrTexture.map(
                id -> ExposureClient.getExposureStorage().getOrQuery(id).map(ExposureSavedData::getType)
                        .orElse(FilmType.BLACK_AND_WHITE),
                texture -> (texture.getPath().endsWith("_black_and_white") || texture.getPath()
                        .endsWith("_bw")) ? FilmType.COLOR : FilmType.BLACK_AND_WHITE);

        @Nullable ExposureImage exposure = idOrTexture.map(
                id -> ExposureClient.getExposureStorage().getOrQuery(id).map(data -> new ExposureImage(id, data)).orElse(null),
                texture -> {
                    @Nullable ExposureTexture exposureTexture = ExposureTexture.getTexture(texture);
                    if (exposureTexture != null)
                        return new ExposureImage(texture.toString(), exposureTexture);
                    else
                        return null;
                }
        );

        if (exposure == null) {
            return;
        }

        int width = exposure.getWidth();
        int height = exposure.getHeight();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(Math.round(x + this.width / 2f), Math.round(y + this.height / 2f), 0);
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.pose().translate(-Math.round(width / 2f), -Math.round(height / 2f), 0);

        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, TEXTURE);

            guiGraphics.pose().pushPose();
            float scale = Math.max((float) width / (FRAME_SIZE), (float) height / (FRAME_SIZE));
            guiGraphics.pose().scale(scale, scale, scale);
            guiGraphics.pose().translate(-12, -12, 0);

            GuiUtil.blit(guiGraphics.pose(), 0, 0, BG_SIZE, BG_SIZE, 0, 0, 256, 256, 0);

            RenderSystem.setShaderColor(type.filmR, type.filmG, type.filmB, type.filmA);
            GuiUtil.blit(guiGraphics.pose(), 0, 0, BG_SIZE, BG_SIZE, 0, BG_SIZE, 256, 256, 0);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            guiGraphics.pose().popPose();
        }

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        ExposureClient.getExposureRenderer().render(idOrTexture, true, true, guiGraphics.pose(), bufferSource,
                0, 0, width, height, 0, 0, 1, 1, LightTexture.FULL_BRIGHT,
                type.frameR, type.frameG, type.frameB, 255);
        bufferSource.endBatch();

        guiGraphics.pose().popPose();
    }
}
