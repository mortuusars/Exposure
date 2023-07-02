package io.github.mortuusars.exposure.camera.component;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CompositionGuide {
    private final String id;
    private final ResourceLocation texture;

    public CompositionGuide(String id, ResourceLocation texture) {
        this.id = id;
        this.texture = texture;
    }

    public String getId() {
        return id;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public Component translate() {
        return Component.translatable("gui." + Exposure.ID + ".composition_guide." + id);
    }
}
