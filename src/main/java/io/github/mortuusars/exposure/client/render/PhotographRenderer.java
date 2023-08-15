package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PhotographRenderer {
    public static final int SIZE = 256;
    public static final ResourceLocation PHOTOGRAPH_TEXTURE = Exposure.resource("textures/block/photograph.png");

    public static void render(Either<String, ResourceLocation> idOrTexture, PoseStack poseStack, MultiBufferSource bufferSource,
                              float minX, float minY, float maxX, float maxY,
                              float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        idOrTexture.ifLeft(id -> Exposure.getStorage().getOrQuery(id).ifPresent(exposureData ->
                        ExposureClient.getExposureRenderer().render(id, exposureData, poseStack, bufferSource,
                                minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a)))
                   .ifRight(resource -> renderTexture(resource, poseStack, bufferSource,
                           minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a));
    }

    public static void render(Either<String, ResourceLocation> idOrTexture, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        render(idOrTexture, poseStack, bufferSource, 0, 0, SIZE, SIZE, 0, 0, 1, 1, packedLight, 255, 255, 255, 255);
    }

    public static void renderOnPaper(Either<String, ResourceLocation> idOrTexture, PoseStack poseStack,
                                     MultiBufferSource bufferSource, int packedLight, int r, int g, int b, int a) {
        poseStack.pushPose();

        renderTexture(PhotographRenderer.PHOTOGRAPH_TEXTURE, poseStack, bufferSource,
                0, 0, PhotographRenderer.SIZE, PhotographRenderer.SIZE, 0, 0, 1, 1,
                packedLight, r, g, b, a);

        // Backside:
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
        poseStack.translate(-SIZE, 0, -0.5);
        renderTexture(PhotographRenderer.PHOTOGRAPH_TEXTURE, poseStack, bufferSource,
                0, 0, PhotographRenderer.SIZE, PhotographRenderer.SIZE, 1, 0, 0, 1,
                packedLight,
                ((int) Mth.clamp(r * 0.85f, 0, 255)),
                ((int) Mth.clamp(g * 0.85f, 0, 255)),
                ((int) Mth.clamp(b * 0.85f, 0, 255)), a);
        poseStack.popPose();

        float offset = SIZE * 0.05f;
        poseStack.translate(offset, offset, 0.2);
        poseStack.scale(0.90f, 0.90f, 0.90f);
        render(idOrTexture, poseStack, bufferSource, 0, 0, SIZE, SIZE, 0, 0, 1, 1, packedLight, r, g, b, a);
        poseStack.popPose();
    }

    public static void renderOnPaper(Either<String, ResourceLocation> idOrTexture, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        renderOnPaper(idOrTexture, poseStack, bufferSource, packedLight, 255, 255, 255, 255);
    }

    public static void renderTexture(ResourceLocation resource, PoseStack poseStack, MultiBufferSource bufferSource,
                                     float minX, float minY, float maxX, float maxY,
                                     float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        RenderSystem.setShaderTexture(0, resource);
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer bufferBuilder = bufferSource.getBuffer(RenderType.text(resource));
        bufferBuilder.vertex(matrix, minX, maxY, 0).color(r,g,b,a).uv(minU, maxV).uv2(packedLight).endVertex();
        bufferBuilder.vertex(matrix, maxX, maxY, 0).color(r,g,b,a).uv(maxU, maxV).uv2(packedLight).endVertex();
        bufferBuilder.vertex(matrix, maxX, minY, 0).color(r,g,b,a).uv(maxU, minV).uv2(packedLight).endVertex();
        bufferBuilder.vertex(matrix, minX, minY, 0).color(r,g,b,a).uv(minU, minV).uv2(packedLight).endVertex();
    }
}
