package io.github.mortuusars.exposure.client.render.image;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImageIdentifier;
import io.github.mortuusars.mortaar.util.color.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ImageRenderer implements AutoCloseable {
    private final Map<RenderableImageIdentifier, RenderedImageInstance> cache = new HashMap<>();

    public void render(RenderableImage image, PoseStack poseStack, MultiBufferSource bufferSource, RenderCoordinates coords, Color color) {
        this.render(image, poseStack, bufferSource, coords, LightTexture.FULL_BRIGHT, color);
    }

    public void render(RenderableImage image, PoseStack poseStack, MultiBufferSource bufferSource, RenderCoordinates coords,
                       int packedLight, Color color) {
        this.render(image, poseStack, bufferSource,
                coords.minX(), coords.minY(), coords.maxX(), coords.maxY(), coords.minU(), coords.minV(), coords.maxU(), coords.maxV(),
                packedLight, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public void render(RenderableImage image, PoseStack poseStack, MultiBufferSource bufferSource, RenderCoordinates coords,
                       int packedLight, int r, int g, int b, int a) {
        this.render(image, poseStack, bufferSource,
                coords.minX(), coords.minY(), coords.maxX(), coords.maxY(), coords.minU(), coords.minV(), coords.maxU(), coords.maxV(),
                packedLight, r, g, b, a);
    }

    public void render(RenderableImage image, PoseStack poseStack, MultiBufferSource bufferSource,
                       float minX, float minY, float maxX, float maxY,
                       float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        getOrCreateInstance(image)
                .draw(poseStack, bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    private RenderedImageInstance getOrCreateInstance(RenderableImage image) {
        return (this.cache).compute(image.getIdentifier(), (id, expData) -> {
            if (expData == null) {
                return new RenderedImageInstance(image);
            }
            expData.replaceData(image);
            return expData;
        });
    }

    public void clearCache() {
        cache.values().forEach(RenderedImageInstance::close);
        cache.clear();
    }

    public void clearCacheOf(String baseID) {
        cache.entrySet().removeIf(entry -> {
            boolean shouldRemove = entry.getKey().base().equals(baseID);
            if (shouldRemove) {
                entry.getValue().close();
            }
            return shouldRemove;
        });
    }

    public void clearCacheOf(Predicate<RenderableImageIdentifier> predicate) {
        cache.entrySet().removeIf(entry -> {
            boolean shouldRemove = predicate.test(entry.getKey());
            if (shouldRemove) {
                entry.getValue().close();
            }
            return shouldRemove;
        });
    }

    @Override
    public void close() {
        clearCache();
    }
}
