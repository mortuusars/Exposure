package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.Codecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record ColorBalance(float r, float g, float b) {
    public ColorBalance {
        Preconditions.checkArgument(r >= -1 && r <= 1, "r must be in -1 to 1 range.");
        Preconditions.checkArgument(g >= -1 && g <= 1, "g must be in -1 to 1 range.");
        Preconditions.checkArgument(b >= -1 && b <= 1, "b must be in -1 to 1 range.");
    }

    public static final ColorBalance EMPTY = new ColorBalance(0, 0, 0);

    public static final Codec<ColorBalance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.floatRange(-1, 1).optionalFieldOf("red", 0f).forGetter(ColorBalance::r),
            Codecs.floatRange(-1, 1).optionalFieldOf("green", 0f).forGetter(ColorBalance::g),
            Codecs.floatRange(-1, 1).optionalFieldOf("blue", 0f).forGetter(ColorBalance::b)
    ).apply(instance, ColorBalance::new));

    public static final StreamCodec<FriendlyByteBuf, ColorBalance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ColorBalance::r,
            ByteBufCodecs.FLOAT, ColorBalance::g,
            ByteBufCodecs.FLOAT, ColorBalance::b,
            ColorBalance::new
    );

    // --


    @Override
    public @NotNull String toString() {
        return "R:%s, G:%s, B:%s".formatted(r, g, b);
    }
}
