package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.client.image.ResourceImage;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RenderedExposures implements AutoCloseable {
    private final Map<String, RenderedExposureInstance> cache = new HashMap<>();

    public RenderableImage getOrCreate(Frame frame) {
        RenderableImage image = getOrCreateRaw(frame.identifier());
        return Censor.isAllowedToRender(frame)
              ? image
              : image.modifyWith(ImageEffect.CENSORED);
    }

    public RenderableImage getOrCreateRaw(ExposureIdentifier identifier) {
        return identifier.map(this::getOrCreateRaw, ResourceImage::getOrCreate);
    }

    public RenderableImage getOrCreateRaw(String id) {
        @Nullable RenderedExposureInstance existingImage = cache.get(id);
        if (existingImage != null) {
            existingImage.setLastAccess(UnixTimestamp.Milliseconds.now());
            return existingImage.getImage();
        }

        return ExposureClient.exposureStore().getOrRequest(id).map(
              exposure -> {
                  RenderableImage image = RenderableImage.of(id, imageFromExposure(exposure));
                  cache.put(id, new RenderedExposureInstance(image, UnixTimestamp.Milliseconds.now()));
                  return image;
              },
              RenderableImage.EMPTY);
    }

    public void clearCacheOf(String id) {
        cache.remove(id);
    }

    public void clearCache() {
        cache.clear();
    }

    public void clearStale() {
        cache.entrySet().removeIf(entry -> {
            boolean stale = UnixTimestamp.Milliseconds.now() - entry.getValue().getLastAccess() > 120_000; // 2 minutes
            if (stale) {
                ExposureClient.imageRenderer().clearCacheOf(entry.getKey());
            }
            return stale;
        });
    }

    @Override
    public void close() {
        clearCache();
    }

    private @NotNull Image imageFromExposure(ExposureData exposure) {
        ColorPalette palette = ColorPalettes.get(Minecrft.levelOrThrow().registryAccess(), exposure.getPaletteId()).value();
        return new PalettedImage(exposure.getWidth(), exposure.getHeight(), exposure.getPixels(), palette);
    }

    public void addDebugInfo(Consumer<String> lines) {
        lines.accept("Cache: " + cache.size());
    }

    private static class RenderedExposureInstance {
        private final RenderableImage image;

        private long lastAccess;

        public RenderedExposureInstance(RenderableImage image, long lastAccess) {
            this.image = image;
            this.lastAccess = lastAccess;
        }

        public RenderableImage getImage() {
            return image;
        }

        public long getLastAccess() {
            return lastAccess;
        }

        public void setLastAccess(long lastAccess) {
            this.lastAccess = lastAccess;
        }
    }
}
