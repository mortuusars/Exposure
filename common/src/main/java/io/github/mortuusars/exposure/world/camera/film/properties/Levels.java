package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record Levels(int shadows, int midtones, int highlights, int black, int white) {
    public Levels {
        checkRange(shadows, "shadows");
        checkRange(midtones, "midtones");
        checkRange(highlights, "highlights");
        checkRange(black, "black");
        checkRange(white, "white");
    }

    private static void checkRange(int value, String name) {
        Preconditions.checkArgument(value >= 0 && value <= 255, name + " '" + value + "' is not valid. 0-255.");
    }

    public static final Levels EMPTY = new Levels(0, 128, 255, 0, 255);

    public static final Codec<Levels> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("shadows", 0).forGetter(Levels::shadows),
            Codec.INT.optionalFieldOf("midtones", 128).forGetter(Levels::midtones),
            Codec.INT.optionalFieldOf("highlights", 255).forGetter(Levels::highlights),
            Codec.INT.optionalFieldOf("black", 0).forGetter(Levels::black),
            Codec.INT.optionalFieldOf("white", 255).forGetter(Levels::white)
    ).apply(instance, Levels::new));

    public static final StreamCodec<FriendlyByteBuf, Levels> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Levels::shadows,
            ByteBufCodecs.VAR_INT, Levels::midtones,
            ByteBufCodecs.VAR_INT, Levels::highlights,
            ByteBufCodecs.VAR_INT, Levels::black,
            ByteBufCodecs.VAR_INT, Levels::white,
            Levels::new
    );

    @Override
    public @NotNull String toString() {
        return "%s, %s, %s | %s, %s".formatted(shadows, midtones, highlights, black, white);
    }
}
