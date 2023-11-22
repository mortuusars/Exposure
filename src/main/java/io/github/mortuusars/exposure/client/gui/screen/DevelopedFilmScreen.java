package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.util.GuiUtil;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

public class DevelopedFilmScreen extends Screen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/developed_film.png");
    public static final int PIECE_WIDTH = 78;
    public static final int PIECE_HEIGHT = 84;
    private final ItemAndStack<DevelopedFilmItem> film;
//    private final List<FrameData> frames;

    private float x;
    private float y;
    private float zoom = 1f;

//    private final float filmWidth;

    public DevelopedFilmScreen(ItemAndStack<DevelopedFilmItem> film) {
        super(Component.empty());
        this.film = film;
        throw new NotImplementedException("This is not working yet. And I'm not sure it ever will.");
//        frames = film.getItem().getExposedFrames(film.getStack());
//        filmWidth = frames.size() * PIECE_WIDTH + PIECE_WIDTH;
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft = Minecraft.getInstance();

        x = width / 2f;
        y = height / 2f;
    }

//    @Override
//    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
////        renderBackground(poseStack);
//        fillGradient(poseStack, 0, 0, width, height, 0x20202020, 0x20121212);
//        super.render(poseStack, mouseX, mouseY, partialTick);
////
////        float scale = (height - (height / 6f)) / phHeight;
////        scale += zoom;
//
//        poseStack.pushPose();
//
//        // Move to center
//        poseStack.translate(x, y, 0);
//        // Scale
//        poseStack.scale(zoom, zoom, zoom);
//        // Set origin point to center (for scale)
////        poseStack.translate(filmWidth / -2f, -(PIECE_HEIGHT / 2f), 0);
//
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.enableBlend();
//        RenderSystem.setShaderTexture(0, TEXTURE);
//
//        if (film.getItem().getType() == FilmType.COLOR)
//            RenderSystem.setShaderColor(1.1F, 0.86F, 0.66F, 0.6F);
//        else
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
//
//        GuiUtil.blit(poseStack, 0f, PIECE_WIDTH, 0f, PIECE_HEIGHT, 0f, 0f, PIECE_WIDTH / 256f, 0f, PIECE_HEIGHT / 256f);
//
////        blit(poseStack, 0, 0, 0, 0, 78, 84);
//
////        for (int i = 0; i < frames.size(); i++) {
////            final int frameX = i * PIECE_WIDTH + PIECE_WIDTH;
////            GuiUtil.blit(poseStack, frameX, (frameX + PIECE_WIDTH), 0f, PIECE_HEIGHT, 0, (i == (frames.size() - 1) ? 156 : PIECE_WIDTH) / 256f, ((i == (frames.size() - 1) ? 156 : PIECE_WIDTH) + PIECE_WIDTH) / 256f, 0f, PIECE_HEIGHT / 256f);
//////            blit(poseStack, frameX, 0,  i == (frames.size() - 1) ? 156 : 78, 0, 78, 84);
////        }
////
////        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
////
////        float size = 70;
////
////        for (int i = 0; i < frames.size(); i++) {
////            FrameData frame = frames.get(i);
////            final int frameX = i * PIECE_WIDTH + PIECE_WIDTH;
////
////            poseStack.pushPose();
////            poseStack.translate(frameX + 4, 7, 0);
////            Exposure.getStorage().getOrQuery(frame.id).ifPresent(data -> {
////                if (film.getItem().getType() == FilmType.COLOR) {
////                    ExposureClient.getExposureRenderer().renderNegative(frame.id, data, true, poseStack,
////                            size, size, LightTexture.FULL_BRIGHT, 180, 130, 110, 230);
////                } else {
////                    ExposureClient.getExposureRenderer().renderNegative(frame.id, data, true, poseStack,
////                            size, size, LightTexture.FULL_BRIGHT, 255, 255, 255, 215);
////                }
////
////            });
////            poseStack.popPose();
////        }
//
//        poseStack.popPose();
//    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Preconditions.checkState(minecraft != null);

        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);

        if (!handled) {

        }

        return handled;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        Preconditions.checkState(minecraft != null);

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        final float minZoom = 0.5f;
        final float maxZoom = 5f;
        final float zoomStep = 0.3f;


        float prevZoom = zoom;

        float zoomChange = delta > 0.0 ? zoomStep : -zoomStep;
//        float modifier = Mth.map(zoom, -0.5f, 2f, 1f, 8f);
        zoom = Mth.clamp(zoom + zoom * zoomChange, minZoom, maxZoom);

        if (zoom != prevZoom) {
            float screenCenterX = width / 2f;
            float screenCenterY = height / 2f;

            double offsetX = mouseX - screenCenterX;
            double offsetY = mouseY - screenCenterY;

            x = (float) (x + (delta > 0.0 ? -(offsetX / zoom) : (offsetX / zoom)));
            y = (float) (y + (delta > 0.0 ? -(offsetY / zoom) : (offsetY / zoom)));
        }


//        if (zoom > minZoom & zoom < maxZoom) {
//            double moveDelta = (zoomChange * modifier) * -1;
//            x = (float) Mth.lerp(moveDelta, x, mouseX);
//            y = (float) Mth.lerp(moveDelta, y, mouseY);
//        }

        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {

    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int button, double pDragX, double pDragY) {
        boolean handled = super.mouseDragged(pMouseX, pMouseY, button, pDragX, pDragY);

        if (!handled && button == 0 || button == 1) {
            this.x += pDragX;
            this.y += pDragY;
//
//            this.x = Mth.clamp(x, -(filmWidth * 0.8f), width + filmWidth * 0.8f);
//            this.y = Mth.clamp(y, 78, (height + 50) * zoom);
            handled = true;
        }

        return handled;
    }
}
