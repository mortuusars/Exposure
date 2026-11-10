package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.Codecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record HSB(float hue, float saturation, float brightness) {
    public HSB {
        Preconditions.checkArgument(hue >= -1 && hue <= 1, "h must be in -1 to 1 range.");
        Preconditions.checkArgument(saturation >= -1 && saturation <= 1, "s must be in -1 to 1 range.");
        Preconditions.checkArgument(brightness >= -1 && brightness <= 1, "b must be in -1 to 1 range.");
    }

    public static final HSB EMPTY = new HSB(0, 0, 0);

    public static final Codec<HSB> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.floatRange(-1, 1).optionalFieldOf("hue", 0f).forGetter(HSB::hue),
            Codecs.floatRange(-1, 1).optionalFieldOf("saturation", 0f).forGetter(HSB::saturation),
            Codecs.floatRange(-1, 1).optionalFieldOf("brightness", 0f).forGetter(HSB::brightness)
    ).apply(instance, HSB::new));

    public static final StreamCodec<FriendlyByteBuf, HSB> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, HSB::hue,
            ByteBufCodecs.FLOAT, HSB::saturation,
            ByteBufCodecs.FLOAT, HSB::brightness,
            HSB::new
    );

    // --

    @Override
    public @NotNull String toString() {
        return "H:%s, S:%s, B:%s".formatted(hue, saturation, brightness);
    }
}
