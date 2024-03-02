package io.github.mortuusars.exposure.render;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.render.modifiers.ExposurePixelModifiers;
import io.github.mortuusars.exposure.render.modifiers.IPixelModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PhotographRenderProperties {
    public static final ResourceLocation EMPTY_TEXTURE = Exposure.resource("textures/empty.png");

    public static final PhotographRenderProperties DEFAULT = new PhotographRenderProperties("default",
            stack -> stack.getItem() instanceof PhotographItem,
            Exposure.resource("textures/photograph/photograph.png"),
            EMPTY_TEXTURE,
            Exposure.resource("textures/photograph/photograph_album.png"),
            EMPTY_TEXTURE,
            ExposurePixelModifiers.EMPTY);

    public static final PhotographRenderProperties AGED = new PhotographRenderProperties("aged",
            stack -> stack.is(Exposure.Items.AGED_PHOTOGRAPH.get()),
            Exposure.resource("textures/photograph/aged_photograph.png"),
            Exposure.resource("textures/photograph/aged_photograph_overlay.png"),
            Exposure.resource("textures/photograph/aged_photograph_album.png"),
            Exposure.resource("textures/photograph/aged_photograph_album_overlay.png"),
            ExposurePixelModifiers.AGED);

    private static final List<PhotographRenderProperties> registeredProperties = new ArrayList<>();

    private final String id;
    private final Predicate<ItemStack> stackPredicate;
    private final ResourceLocation paperTexture;
    private final ResourceLocation overlayTexture;
    private final ResourceLocation albumPaperTexture;
    private final ResourceLocation albumPaperOverlayTexture;
    private final IPixelModifier modifier;

    static {
        register(PhotographRenderProperties.AGED);
    }

    public PhotographRenderProperties(String id, Predicate<ItemStack> stackPredicate, ResourceLocation paperTexture,
                                      ResourceLocation overlayTexture, ResourceLocation albumPaperTexture,
                                      ResourceLocation albumPaperOverlayTexture, IPixelModifier modifier) {
        this.id = id;
        this.stackPredicate = stackPredicate;
        this.paperTexture = paperTexture;
        this.overlayTexture = overlayTexture;
        this.albumPaperTexture = albumPaperTexture;
        this.albumPaperOverlayTexture = albumPaperOverlayTexture;
        this.modifier = modifier;
    }

    public boolean matches(ItemStack stack) {
        return stackPredicate.test(stack);
    }

    public String getId() {
        return id;
    }

    public ResourceLocation getPaperTexture() {
        return paperTexture;
    }

    public ResourceLocation getPaperOverlayTexture() {
        return overlayTexture;
    }

    public ResourceLocation getAlbumPaperTexture() {
        return albumPaperTexture;
    }

    public ResourceLocation getAlbumPaperOverlayTexture() {
        return albumPaperOverlayTexture;
    }

    public IPixelModifier getModifier() {
        return modifier;
    }

    public boolean hasPaperOverlayTexture() {
        return !getPaperOverlayTexture().equals(EMPTY_TEXTURE);
    }
    public boolean hasAlbumPaperOverlayTexture() {
        return !getAlbumPaperOverlayTexture().equals(EMPTY_TEXTURE);
    }

    public static void register(PhotographRenderProperties renderProperties) {
        for (int i = PhotographRenderProperties.registeredProperties.size() - 1; i >= 0; i--) {
            PhotographRenderProperties properties = PhotographRenderProperties.registeredProperties.get(i);
            if (properties.getId().equals(renderProperties.getId()))
                PhotographRenderProperties.registeredProperties.remove(i);
        }

        PhotographRenderProperties.registeredProperties.add(0, renderProperties);
    }

    public static PhotographRenderProperties get(ItemStack stack) {
        for (PhotographRenderProperties properties : registeredProperties) {
            if (properties.matches(stack))
                return properties;
        }

        return PhotographRenderProperties.DEFAULT;
    }
}
