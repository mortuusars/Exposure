package io.github.mortuusars.exposure.client.render.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.mortaar.util.color.Color;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class TextureRenderer {
    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture,
                              int packedLight, Color color) {
        render(poseStack, bufferSource, texture, packedLight, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture,
                              int packedLight, int r, int g, int b, int a) {
        render(poseStack, bufferSource, texture, 0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture,
                              float x, float y, float width, float height, int packedLight, int r, int g, int b, int a) {
        render(poseStack, bufferSource, texture, x, y, x + width, y + height,
                0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture,
                              float minX, float minY, float maxX, float maxY,
                              float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer bufferBuilder = bufferSource.getBuffer(RenderType.text(texture));
        bufferBuilder.addVertex(matrix, minX, maxY, 0).setColor(r, g, b, a).setUv(minU, maxV).setLight(packedLight);
        bufferBuilder.addVertex(matrix, maxX, maxY, 0).setColor(r, g, b, a).setUv(maxU, maxV).setLight(packedLight);
        bufferBuilder.addVertex(matrix, maxX, minY, 0).setColor(r, g, b, a).setUv(maxU, minV).setLight(packedLight);
        bufferBuilder.addVertex(matrix, minX, minY, 0).setColor(r, g, b, a).setUv(minU, minV).setLight(packedLight);
    }
}
