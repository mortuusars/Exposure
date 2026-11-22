package io.github.mortuusars.exposure.client.render.photograph;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record PhotographStyle(Identifier paperTexture,
                              Identifier overlayTexture,
                              Identifier albumPaperTexture,
                              Identifier albumOverlayTexture,
                              ImageEffect modifier) {

    public static final PhotographStyle REGULAR = new PhotographStyle(
            ExposureClient.Textures.Photograph.REGULAR_PAPER,
            ExposureClient.Textures.EMPTY,
            ExposureClient.Textures.Photograph.REGULAR_ALBUM_PAPER,
            ExposureClient.Textures.EMPTY,
            ImageEffect.EMPTY);

    public static PhotographStyle of(ItemStack photographStack) {
        return photographStack.getItem() instanceof PhotographItem photographItem
                ? PhotographStyles.get(photographItem.getType(photographStack))
                : PhotographStyle.REGULAR;
    }

    public boolean hasOverlayTexture() {
        return !overlayTexture().equals(ExposureClient.Textures.EMPTY);
    }

    public boolean hasAlbumOverlayTexture() {
        return !albumOverlayTexture().equals(ExposureClient.Textures.EMPTY);
    }

    public RenderableImage process(RenderableImage image) {
        return !modifier.equals(ImageEffect.EMPTY) ? image.modifyWith(modifier) : image;
    }
}
