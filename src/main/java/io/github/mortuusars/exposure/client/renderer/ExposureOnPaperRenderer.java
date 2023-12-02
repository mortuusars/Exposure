package io.github.mortuusars.exposure.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class ExposureOnPaperRenderer {
    public static final ResourceLocation PHOTOGRAPH_TEXTURE = Exposure.resource("textures/block/photograph.png");
    public static final int SIZE = 256;

    private final NewExposureRenderer exposureRenderer;

    public ExposureOnPaperRenderer(NewExposureRenderer exposureRenderer) {
        this.exposureRenderer = exposureRenderer;
    }

    public void render(@NotNull Either<String, ResourceLocation> idOrTexture, boolean negative, boolean simulateFilm,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a,
                       boolean renderBackside) {
        renderTexture(PHOTOGRAPH_TEXTURE, poseStack, bufferSource,
                0, 0, SIZE, SIZE, 0, 0, 1, 1,
                packedLight, r, g, b, a);

        if (renderBackside) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(-SIZE, 0, -0.5);

            renderTexture(PHOTOGRAPH_TEXTURE, poseStack, bufferSource,
                    0, 0, SIZE, SIZE, 1, 0, 0, 1,
                    packedLight, (int)(r * 0.85f), (int)(g * 0.85f), (int)(b * 0.85f), a);

            poseStack.popPose();
        }

        poseStack.pushPose();
        float offset = SIZE * 0.0625f;
        poseStack.translate(offset, offset, 0.2);
        poseStack.scale(0.875f, 0.875f, 0.875f);
        exposureRenderer.render(idOrTexture, negative, simulateFilm, poseStack, bufferSource,
                minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
        poseStack.popPose();
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
