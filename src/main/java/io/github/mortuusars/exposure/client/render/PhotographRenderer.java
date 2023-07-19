package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.camera.Photograph;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;

public class PhotographRenderer {
    public static final int SIZE = 256;
    public static void render(Photograph photograph, PoseStack poseStack, int packedLight) {
        photograph.getIdOrResource()
                .ifLeft(id -> renderExposure(id, poseStack, packedLight))
                .ifRight(resource -> renderTexture(resource, poseStack, packedLight));
    }

    public static void render(Photograph photograph, PoseStack poseStack) {
        photograph.getIdOrResource()
                .ifLeft(id -> renderExposure(id, poseStack, LightTexture.FULL_BRIGHT))
                .ifRight(resource -> renderTexture(resource, poseStack, LightTexture.FULL_BRIGHT));
    }

    public static void renderExposure(String id, PoseStack poseStack, int packedLight) {
        Exposure.getStorage().getOrQuery(id).ifPresent(exposureData -> {
            ExposureClient.getExposureRenderer().render(id, exposureData, false, poseStack, packedLight, SIZE, SIZE);
        });
    }

    public static void renderTexture(ResourceLocation resource, PoseStack poseStack, int packedLight) {
        RenderSystem.setShaderTexture(0, resource);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        GuiComponent.blit(poseStack, 0, 0, 0, 0, SIZE, SIZE, SIZE, SIZE);
        drawTextureQuad(poseStack, 0, 0, SIZE, SIZE, 0, 0, 0, 1, 1, packedLight);
    }

    private static void drawTextureQuad(PoseStack poseStack, float minX, float minY, float maxX, float maxY, float blitOffset,
                                        float minU, float minV, float maxU, float maxV, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix, minX, maxY, blitOffset).uv(minU, maxV).uv2(packedLight).endVertex();
        bufferbuilder.vertex(matrix, maxX, maxY, blitOffset).uv(maxU, maxV).uv2(packedLight).endVertex();
        bufferbuilder.vertex(matrix, maxX, minY, blitOffset).uv(maxU, minV).uv2(packedLight).endVertex();
        bufferbuilder.vertex(matrix, minX, minY, blitOffset).uv(minU, minV).uv2(packedLight).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}
