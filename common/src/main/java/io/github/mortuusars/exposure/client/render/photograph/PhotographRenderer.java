package io.github.mortuusars.exposure.client.render.photograph;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.render.texture.TextureRenderer;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PhotographRenderer {
    public boolean render(ItemStack itemStack, boolean renderPaper, boolean renderBackside, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight) {
        return render(itemStack, renderPaper, renderBackside, poseStack, bufferSource, packedLight, 255, 255, 255, 255);
    }

    public boolean render(ItemStack itemStack, boolean renderPaper, boolean renderBackside, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, int r, int g, int b, int a) {
        if (itemStack.getItem() instanceof PhotographItem photographItem)
            return renderPhotograph(poseStack, bufferSource, photographItem, itemStack, renderPaper, renderBackside, packedLight, r, g, b, a);
        else if (itemStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem)
            return renderStackedPhotographs(stackedPhotographsItem, itemStack, poseStack, bufferSource, packedLight, r, g, b, a);
        return false;
    }

    public boolean renderPhotograph(PoseStack poseStack, MultiBufferSource bufferSource,
                                    PhotographItem photographItem, ItemStack photographStack,
                                    boolean renderPaper, boolean renderBackside, int packedLight, int r, int g, int b, int a) {

        PhotographStyle style = PhotographStyle.of(photographStack);

        Frame frame = photographItem.getFrame(photographStack);

        return renderPhotograph(poseStack, bufferSource, style, frame, renderPaper, renderBackside, packedLight, r, g, b, a);
    }

    public boolean renderPhotograph(PoseStack poseStack, MultiBufferSource bufferSource,
                                        PhotographStyle style, Frame frame,
                                        boolean renderPaper, boolean renderBackside, int packedLight, int r, int g, int b, int a) {

        RenderableImage image = style.process(ExposureClient.renderedExposures().getOrCreate(frame));

        int paperRotation = frame.identifier().hashCode() % 4 * 90;

        if (renderPaper && style.paperTexture() != ExposureClient.Textures.EMPTY) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(paperRotation));
            poseStack.translate(-0.5f, -0.5f, 0);

            TextureRenderer.render(poseStack, bufferSource, style.paperTexture(), packedLight, r, g, b, a);

            poseStack.popPose();

            if (renderBackside) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-0.5, 0, -0.5);

                poseStack.translate(0.5f, 0.5f, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(paperRotation));
                poseStack.translate(-0.5f, -0.5f, 0);

                TextureRenderer.render(poseStack, bufferSource, style.paperTexture(),
                        packedLight, (int) (r * 0.85f), (int) (g * 0.85f), (int) (b * 0.85f), a);

                poseStack.popPose();
            }
        }

        if (renderPaper) {
            poseStack.pushPose();
            float offset = 0.0625f;
            poseStack.translate(offset, offset, 0.001);
            poseStack.scale(0.875f, 0.875f, 0.875f);
            ExposureClient.imageRenderer().render(image, poseStack, bufferSource, RenderCoordinates.DEFAULT, packedLight, r, g, b, a);
            poseStack.popPose();
        } else {
            ExposureClient.imageRenderer().render(image, poseStack, bufferSource, RenderCoordinates.DEFAULT, packedLight, r, g, b, a);
        }

        if (renderPaper && style.hasOverlayTexture()) {
            poseStack.pushPose();

            poseStack.translate(0.5f, 0.5f, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(paperRotation));
            poseStack.translate(-0.5f, -0.5f, 0);

            poseStack.translate(0, 0, 0.002);
            TextureRenderer.render(poseStack, bufferSource, style.overlayTexture(), packedLight, r, g, b, a);
            poseStack.popPose();
        }

        return !image.isEmpty();
    }

    public boolean renderStackedPhotographs(StackedPhotographsItem stackedPhotographsItem, ItemStack stack,
                                                PoseStack poseStack, MultiBufferSource bufferSource,
                                                int packedLight, int r, int g, int b, int a) {
        List<ItemAndStack<PhotographItem>> photographs = stackedPhotographsItem.getPhotographs(stack);
        return renderStackedPhotographs(photographs, poseStack, bufferSource, packedLight, r, g, b, a);
    }

    public boolean renderStackedPhotographs(List<ItemAndStack<PhotographItem>> photographs,
                                                PoseStack poseStack, MultiBufferSource bufferSource,
                                                int packedLight, int r, int g, int b, int a) {
        if (photographs.isEmpty()) return false;

        boolean photographRendered = false;

        for (int i = 2; i >= 0; i--) {
            if (photographs.size() - 1 < i)
                continue;

            ItemAndStack<PhotographItem> photograph = photographs.get(i);

            // Top photograph:
            if (i == 0) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 0.002);
                photographRendered = renderPhotograph(poseStack, bufferSource, photograph.getItem(), photograph.getItemStack(),
                        true, false, packedLight, r, g, b, a);
                poseStack.popPose();
                break;
            }

            // Photographs below (only paper)
            float posOffset = getStackedPhotographOffset() * i;

            poseStack.pushPose();
            poseStack.translate(posOffset, posOffset, 0.002 - i / 1000f);

            poseStack.translate(0.5f, 0.5f, 0);

            Frame frame = photograph.getItem().getFrame(photograph.getItemStack());
            int rotation = frame.identifier().hashCode() % 4 * 90;
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

            poseStack.translate(-0.5f, -0.5f, 0);

            float brightness = 1f - (getStackedBrightnessStep() * i);

            PhotographStyle photographStyle = PhotographStyle.of(photograph.getItemStack());

            TextureRenderer.render(poseStack, bufferSource, photographStyle.paperTexture(),
                    packedLight, (int)(r * brightness), (int)(g * brightness), (int)(b * brightness), a);

            poseStack.popPose();
        }

        return photographRendered;
    }

    public void renderGui(PhotographItem item, ItemStack stack, GuiGraphics gui, int x, int y, int size) {
        PhotographStyle style = PhotographStyle.of(stack);
        Frame frame = item.getFrame(stack);
        RenderableImage image = style.process(ExposureClient.renderedExposures().getOrCreate(frame));
        ExposureClient.imageRenderer().renderGui(image, gui, x, y, size, size, Color.WHITE);
    }

    public void renderStackedGui(List<ItemAndStack<PhotographItem>> photographs, GuiGraphics gui,
                                 float x, float y, float size) {
        final int paperSize = 64;
        final int inset = 4;
        gui.pose().pushMatrix();
        gui.pose().translate(x, y);
        gui.pose().scale(size / paperSize);
        for (int i = Math.min(2, photographs.size() - 1); i >= 0; i--) {
            ItemAndStack<PhotographItem> photograph = photographs.get(i);
            ItemStack stack = photograph.getItemStack();
            PhotographStyle style = PhotographStyle.of(stack);
            Frame frame = photograph.getItem().getFrame(stack);
            int offset = Math.round(getStackedPhotographOffset() * paperSize * i);
            int brightness = (int) (255 * (1f - getStackedBrightnessStep() * i));
            float rotation = (float) (frame.identifier().hashCode() % 4 * Math.PI / 2);
            drawGuiPaper(gui, style.paperTexture(), offset, paperSize, rotation,
                    Color.rgb(brightness, brightness, brightness).getARGB());

            if (i == 0) {
                renderGui(photograph.getItem(), stack, gui, inset, inset, paperSize - inset * 2);
                if (style.hasOverlayTexture()) {
                    drawGuiPaper(gui, style.overlayTexture(), 0, paperSize, rotation, Color.WHITE.getARGB());
                }
            }
        }
        gui.pose().popMatrix();
    }

    private void drawGuiPaper(GuiGraphics gui, Identifier texture, int offset, int size, float rotation, int color) {
        if (texture.equals(ExposureClient.Textures.EMPTY)) return;
        gui.pose().pushMatrix();
        gui.pose().translate(offset + size / 2f, offset + size / 2f);
        gui.pose().rotate(rotation);
        gui.pose().translate(-size / 2f, -size / 2f);
        gui.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0, 0, size, size, size, size, color);
        gui.pose().popMatrix();
    }

    public float getStackedBrightnessStep() {
        return 0.2f;
    }

    public float getStackedPhotographOffset() {
        // 2 px / Texture size (64px) = 0.03125
        return 0.03125f;
    }
}