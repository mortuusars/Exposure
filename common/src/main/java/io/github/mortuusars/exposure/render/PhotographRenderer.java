package io.github.mortuusars.exposure.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class PhotographRenderer {
    public static void render(ItemStack stack, boolean renderPaper, boolean renderBackside, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, int r, int g, int b, int a) {
        if (stack.getItem() instanceof PhotographItem photographItem)
            renderPhotograph(photographItem, stack, renderPaper, renderBackside, poseStack, bufferSource, packedLight, r, g, b, a);
        else if (stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem)
            renderStackedPhotographs(stackedPhotographsItem, stack, poseStack, bufferSource, packedLight, r, g, b, a);
    }

    public static void renderPhotograph(PhotographItem photographItem, ItemStack stack, boolean renderPaper,
                                        boolean renderBackside, PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedLight, int r, int g, int b, int a) {
        PhotographRenderProperties properties = PhotographRenderProperties.get(stack);
        int size = ExposureClient.getExposureRenderer().getSize();

        if (renderPaper) {
            renderTexture(properties.getPaperTexture(), poseStack, bufferSource, 0, 0,
                    size, size, packedLight, r, g, b, a);

            if (renderBackside) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-size, 0, -0.5);

                renderTexture(properties.getPaperTexture(), poseStack, bufferSource,
                        packedLight, (int) (r * 0.85f), (int) (g * 0.85f), (int) (b * 0.85f), a);

                poseStack.popPose();
            }
        }

        @Nullable Either<String, ResourceLocation> idOrTexture = photographItem.getIdOrTexture(stack);

        if (idOrTexture != null) {
            if (renderPaper) {
                poseStack.pushPose();
                float offset = size * 0.0625f;
                poseStack.translate(offset, offset, 1);
                poseStack.scale(0.875f, 0.875f, 0.875f);
                ExposureClient.getExposureRenderer().render(idOrTexture, properties.getModifier(), poseStack, bufferSource,
                        packedLight, r, g, b, a);
                poseStack.popPose();
            } else {
                ExposureClient.getExposureRenderer().render(idOrTexture, properties.getModifier(), poseStack, bufferSource,
                        packedLight, r, g, b, a);
            }

            if (renderPaper && properties.hasPaperOverlayTexture()) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 2);
                renderTexture(properties.getPaperOverlayTexture(), poseStack, bufferSource, packedLight, r, g, b, a);
                poseStack.popPose();
            }
        }
    }

    public static void renderStackedPhotographs(StackedPhotographsItem stackedPhotographsItem, ItemStack stack,
                                                PoseStack poseStack, MultiBufferSource bufferSource,
                                                int packedLight, int r, int g, int b, int a) {
        List<ItemAndStack<PhotographItem>> photographs = stackedPhotographsItem.getPhotographs(stack, 3);
        renderStackedPhotographs(photographs, poseStack, bufferSource, packedLight, r, g, b, a);
    }

    public static void renderStackedPhotographs(List<ItemAndStack<PhotographItem>> photographs,
                                                PoseStack poseStack, MultiBufferSource bufferSource,
                                                int packedLight, int r, int g, int b, int a) {
        if (photographs.isEmpty())
            return;

        for (int i = 2; i >= 0; i--) {
            if (photographs.size() - 1 < i)
                continue;

            ItemAndStack<PhotographItem> photograph = photographs.get(i);
            PhotographRenderProperties properties = PhotographRenderProperties.get(photograph.getStack());

            // Top photograph:
            if (i == 0) {
                poseStack.pushPose();
                renderPhotograph(photograph.getItem(), photograph.getStack(), true, false, poseStack,
                        bufferSource, packedLight, r, g, b, a);
                poseStack.popPose();
                break;
            }

            // Photographs below (only paper)

            float posOffset = getStackedPhotographOffset() * i;
            float rotateOffset = ExposureClient.getExposureRenderer().getSize() / 2f;

            poseStack.pushPose();
            poseStack.translate(posOffset, posOffset, -i);

            poseStack.translate(rotateOffset, rotateOffset, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * 90 + 90));
            poseStack.translate(-rotateOffset, -rotateOffset, 0);

            float brightnessMul = 1f - (getStackedBrightnessStep() * i);

            renderTexture(properties.getPaperTexture(), poseStack, bufferSource,
                    packedLight, (int)(r * brightnessMul), (int)(g * brightnessMul), (int)(b * brightnessMul), a);

            poseStack.popPose();
        }
    }

    public static float getStackedBrightnessStep() {
        return 0.15f;
    }

    public static float getStackedPhotographOffset() {
        // 2 px / Texture size (64px) = 0.03125
        return ExposureClient.getExposureRenderer().getSize() * 0.03125f;
    }

    private static void renderTexture(ResourceLocation resource, PoseStack poseStack, MultiBufferSource bufferSource,
                                      int packedLight, int r, int g, int b, int a) {
        renderTexture(resource, poseStack, bufferSource, 0, 0, ExposureClient.getExposureRenderer().getSize(),
                ExposureClient.getExposureRenderer().getSize(), packedLight, r, g, b, a);
    }

    private static void renderTexture(ResourceLocation resource, PoseStack poseStack, MultiBufferSource bufferSource,
                                      float x, float y, float width, float height, int packedLight, int r, int g, int b, int a) {
        renderTexture(resource, poseStack, bufferSource, x, y, x + width, y + height,
                0, 0, 1, 1, packedLight, r, g, b, a);
    }

    private static void renderTexture(ResourceLocation resource, PoseStack poseStack, MultiBufferSource bufferSource,
                                      float minX, float minY, float maxX, float maxY,
                                      float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        RenderSystem.setShaderTexture(0, resource);
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer bufferBuilder = bufferSource.getBuffer(RenderType.text(resource));
        bufferBuilder.vertex(matrix, minX, maxY, 0).color(r, g, b, a).uv(minU, maxV).uv2(packedLight).endVertex();
        bufferBuilder.vertex(matrix, maxX, maxY, 0).color(r, g, b, a).uv(maxU, maxV).uv2(packedLight).endVertex();
        bufferBuilder.vertex(matrix, maxX, minY, 0).color(r, g, b, a).uv(maxU, minV).uv2(packedLight).endVertex();
        bufferBuilder.vertex(matrix, minX, minY, 0).color(r, g, b, a).uv(minU, minV).uv2(packedLight).endVertex();
    }
}