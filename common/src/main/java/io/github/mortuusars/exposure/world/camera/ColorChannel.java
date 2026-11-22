package io.github.mortuusars.exposure.world.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum ColorChannel implements StringRepresentable {
    RED(0xFFD8523E),
    GREEN(0xFF7BC64B),
    BLUE(0xFF4E73CE);

    public static final Codec<ColorChannel> CODEC = StringRepresentable.fromEnum(ColorChannel::values);
    public static final StreamCodec<ByteBuf, ColorChannel> STREAM_CODEC =
            ByteBufCodecs.idMapper(id -> ColorChannel.values()[id], ColorChannel::ordinal);

    // Used in UI to color text, etc.
    private final int color;

    ColorChannel(int color) {
        this.color = color;
    }

    public int getRepresentationColor() {
        return color;
    }

    public static Optional<ColorChannel> fromFilterStack(ItemStack stack) {
        if (stack.is(Exposure.Tags.Items.RED_FILTERS))
            return Optional.of(RED);
        else if (stack.is(Exposure.Tags.Items.GREEN_FILTERS))
            return Optional.of(GREEN);
        else if (stack.is(Exposure.Tags.Items.BLUE_FILTERS))
            return Optional.of(BLUE);
        else
            return Optional.empty();
    }

    public static ColorChannel fromStringOrDefault(String serializedName, ColorChannel defaultValue) {
        for (ColorChannel value : values()) {
            if (value.getSerializedName().equals(serializedName))
                return value;
        }
        return defaultValue;
    }

    public static Optional<ColorChannel> fromStringOptional(String serializedName) {
        for (ColorChannel value : values()) {
            if (value.getSerializedName().equals(serializedName))
                return Optional.of(value);
        }
        return Optional.empty();
    }

    public static ColorChannel fromStringOrThrow(String serializedName) {
        for (ColorChannel value : values()) {
            if (value.getSerializedName().equals(serializedName))
                return value;
        }
        throw new IllegalArgumentException(serializedName + " is not a valid ColorChannel.");
    }

    @Override
    public @NotNull String getSerializedName() {
        return toString().toLowerCase();
    }

    public Identifier getShader() {
        return Exposure.resource("post/" + getSerializedName() + "_filter");
    }
}
