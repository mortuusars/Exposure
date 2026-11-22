package io.github.mortuusars.exposure.world.camera.component;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record CompositionGuide(String name) {
    public static final Codec<CompositionGuide> CODEC = Codec.STRING.xmap(CompositionGuides::byNameOrNone, CompositionGuide::name);

    public static final StreamCodec<ByteBuf, CompositionGuide> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CompositionGuide::name,
            CompositionGuides::byNameOrNone
    );

    public MutableComponent translate() {
        return Component.translatable("gui." + Exposure.ID + ".composition_guide." + name);
    }

    public Identifier overlayTextureLocation() {
        return Exposure.resource("textures/gui/viewfinder/composition_guide/" + name + ".png");
    }

    public Identifier buttonSpriteLocation() {
        return Exposure.resource("camera_controls/composition_guide/" + name);
    }
}
