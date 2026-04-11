package io.github.mortuusars.exposure.client.render.image;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import org.joml.Matrix4f;

/**
 * Credits to <a href="https://github.com/Jalvaviel/MapMipMapMod">MapMipMapMod by Jalvaviel</a> for example of mipmap implementation for dynamic images.
 * And to <a href="https://github.com/bravely-beep">bravely-beep</a> for pointing me to it.
 */
public class RenderedImageInstance implements AutoCloseable {
    protected final Identifier textureLocation;
    protected RenderableImage image;
    protected DynamicTexture texture;
    protected final RenderType renderType;
    protected boolean requiresUpload = true;

    RenderedImageInstance(RenderableImage image) {
        this.image = image;
        this.texture = new DynamicTexture(image.getIdentifier().toString(), image.width(), image.height(), true);
        this.textureLocation = image.getIdentifier().toIdentifier();
        Minecraft.getInstance().getTextureManager().register(textureLocation, this.texture);

        renderType = RenderTypes.text(textureLocation);

        forceUpload();
    }

    public void replaceData(RenderableImage image) {
        boolean hasChanged = !image.getIdentifier().equals(this.image.getIdentifier());
        this.image = image;
        if (hasChanged) {
            this.texture = new DynamicTexture(image.getIdentifier().toString(), image.width(), image.height(), true);
            forceUpload();
        }
    }

    public void forceUpload() {
        this.requiresUpload = true;
    }

    protected void updateTexture() {
        if (texture.getPixels() == null) return;

        int width = this.image.width();
        int height = this.image.height();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int ARGB = this.image.getPixelARGB(x, y);
                this.texture.getPixels().setPixel(x, y, Color.ABGRtoARGB(ARGB)); // Texture is in ABGR format
            }
        }

        int mipmapLevel = Minecraft.getInstance().options.mipmapLevels().get();
        if (mipmapLevel > 0 && width > 2 && height > 2) {
            applyMipMap(mipmapLevel, width, height);
        }

        this.texture.upload();
    }

    private void applyMipMap(int mipmapLevel, int width, int height) {
        if (texture.getPixels() == null) return;

        try {
            SpriteContents spriteContents = new SpriteContents(this.textureLocation,
                    new FrameSize(width, height), texture.getPixels());

            spriteContents.increaseMipLevel(mipmapLevel);
            spriteContents.uploadFirstFrame(texture.getTexture(), 0);
        } catch (Exception e) {
            Exposure.LOGGER.error("Failed to generate mipmaps: {}", e.getMessage());
        }
    }

    public void draw(PoseStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                     float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        if (this.requiresUpload) {
            this.updateTexture();
            this.requiresUpload = false;
        }

        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.renderType);
        vertexConsumer.addVertex(matrix4f, minX, maxY, 0).setColor(r, g, b, a).setUv(minU, maxV).setLight(packedLight);
        vertexConsumer.addVertex(matrix4f, maxX, maxY, 0).setColor(r, g, b, a).setUv(maxU, maxV).setLight(packedLight);
        vertexConsumer.addVertex(matrix4f, maxX, minY, 0).setColor(r, g, b, a).setUv(maxU, minV).setLight(packedLight);
        vertexConsumer.addVertex(matrix4f, minX, minY, 0).setColor(r, g, b, a).setUv(minU, minV).setLight(packedLight);
    }

    public void close() {
        Minecraft.getInstance().getTextureManager().release(textureLocation);
        this.texture.close();
    }
}
