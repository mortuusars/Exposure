package io.github.mortuusars.exposure.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.mortuusars.exposure.util.Rect2f;
import net.minecraft.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public class GuiUtil {
    public static void blit(Matrix3x2fStack poseStack, Rect2f rect,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(null, poseStack, rect, u, v, textureWidth, textureHeight, zOffset);
    }

    public static void blit(@Nullable Identifier texture, Matrix3x2fStack poseStack, Rect2f rect,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(texture, poseStack, rect.x, rect.y, rect.width, rect.height, u, v, textureWidth, textureHeight, zOffset);
    }

    public static void blit(Matrix3x2fStack poseStack, float x, float y, float width, float height,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(null, poseStack, x, y, width, height, u, v, textureWidth, textureHeight, zOffset);
    }

    public static void blit(@Nullable Identifier texture, Matrix3x2fStack poseStack, float x, float y, float width, float height,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(texture, poseStack, x, x + width, y, y + height, zOffset,
                u / (float)textureWidth, (u + width) / (float)textureWidth,
                v / (float)textureHeight, (v + height) / (float)textureHeight);
    }

    public static void blit(Matrix3x2fStack poseStack, float minX, float maxX, float minY, float maxY, float zOffset,
                            float minU, float maxU, float minV, float maxV) {
        blit(null, poseStack, minX, maxX, minY, maxY, zOffset, minU, maxU, minV, maxV);
    }

    private static void blit(@Nullable Identifier texture, Matrix3x2fStack poseStack,
                             float minX, float maxX, float minY, float maxY, float zOffset,
                             float minU, float maxU, float minV, float maxV) {
        if (texture != null) {
//            //RenderSystem.setShaderTexture(0, texture);
        }

//        //RenderSystem.setShader(CoreShaders.POSITION);
//        //RenderSystem.setShader(CoreShaders.POSITION_TEX);
//        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//        bufferBuilder.addVertex(matrix, minX, maxY, zOffset).setUv(minU, maxV);
//        bufferBuilder.addVertex(matrix, maxX, maxY, zOffset).setUv(maxU, maxV);
//        bufferBuilder.addVertex(matrix, maxX, minY, zOffset).setUv(maxU, minV);
//        bufferBuilder.addVertex(matrix, minX, minY, zOffset).setUv(minU, minV);
//        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    // --

    public static void drawRect(GuiGraphics guiGraphics, Rect2f rect, int color) {
        drawRect(guiGraphics, rect.x, rect.y, rect.width, rect.height, color);
    }

    public static void drawRect(GuiGraphics guiGraphics, float x, float y, float width, float height, int color) {
        drawRect(guiGraphics.pose(), x, y, x + width, y + height, color);
    }

    public static void drawRect(Matrix3x2fStack poseStack, float minX, float minY, float maxX, float maxY, int color) {
        if (minX < maxX) {
            float temp = minX;
            minX = maxX;
            maxX = temp;
        }

        if (minY < maxY) {
            float temp = minY;
            minY = maxY;
            maxY = temp;
        }

//        Matrix4f matrix = poseStack.last().pose();
////        //RenderSystem.setShader(CoreShaders.POSITION_COLOR);
//        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
//        bufferBuilder.addVertex(matrix, minX, maxY, 0).setColor(color);
//        bufferBuilder.addVertex(matrix, maxX, maxY, 0).setColor(color);
//        bufferBuilder.addVertex(matrix, maxX, minY, 0).setColor(color);
//        bufferBuilder.addVertex(matrix, minX, minY, 0).setColor(color);
//        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    // --

    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int width, int color) {
        renderScrollingString(guiGraphics, font, text, x, y, x + width, y + font.lineHeight, color);
    }

    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int maxY, int color) {
        renderScrollingString(guiGraphics, font, text, (minX + maxX) / 2, minX, minY, maxX, maxY, color);
    }

    // Doesn't work in toast for some reason.
    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int centerX, int minX, int minY, int maxX, int maxY, int color) {
        int fontWidth = font.width(text);
        int y = (minY + maxY - 9) / 2 + 1;
        int width = maxX - minX;
        if (fontWidth > width) {
            int remaining = fontWidth - width;
            double d = (double) Util.getMillis() / 400;
            double e = Math.max((double)remaining * 0.5, 3.0);
            double f = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d / e)) / 2.0 + 0.5;
            double g = Mth.lerp(f, 0.0, remaining);
            guiGraphics.enableScissor(minX, minY, maxX, maxY);
            guiGraphics.drawString(font, text, minX - (int)g, y, color, false);
            guiGraphics.disableScissor();
        } else {
            int l = Mth.clamp(centerX, minX + fontWidth / 2, maxX - fontWidth / 2);
            guiGraphics.drawCenteredString(font, text, l, y, color);
        }
    }
}
