package io.github.mortuusars.exposure.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public class GuiUtil {
    public static void blit(PoseStack poseStack, float minX, float maxX, float minY, float maxY, float blitOffset, float minU, float maxU, float minV, float maxV) {
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix, minX, maxY, blitOffset).uv(minU, maxV).endVertex();
        bufferbuilder.vertex(matrix, maxX, maxY, blitOffset).uv(maxU, maxV).endVertex();
        bufferbuilder.vertex(matrix, maxX, minY, blitOffset).uv(maxU, minV).endVertex();
        bufferbuilder.vertex(matrix, minX, minY, blitOffset).uv(minU, minV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    public static void blit(PoseStack poseStack, float x, float y, float width, float height, int u, int v, int textureWidth, int textureHeight, float blitOffset) {
        blit(poseStack, x, x + width, y, y + height, blitOffset,
                (u + 0.0F) / (float)textureWidth, (u + width) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + height) / (float)textureHeight);
    }
}
