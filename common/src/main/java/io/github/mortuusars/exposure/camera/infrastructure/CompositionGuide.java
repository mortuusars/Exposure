package io.github.mortuusars.exposure.camera.infrastructure;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

@SuppressWarnings("ClassCanBeRecord")
public class CompositionGuide {
    private final String id;

    public CompositionGuide(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Component translate() {
        return Component.translatable("gui." + Exposure.ID + ".composition_guide." + id);
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
    }

    public static CompositionGuide fromBuffer(FriendlyByteBuf buffer) {
        return CompositionGuides.byIdOrNone(buffer.readUtf());
    }
}
