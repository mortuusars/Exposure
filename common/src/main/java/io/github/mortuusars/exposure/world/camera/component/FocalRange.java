package io.github.mortuusars.exposure.world.camera.component;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.util.Fov;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class FocalRange implements StringRepresentable {
    public static final Codec<FocalRange> RANGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min").forGetter(FocalRange::min),
            Codec.INT.fieldOf("max").forGetter(FocalRange::max)
    ).apply(instance, FocalRange::new));

    public static final Codec<FocalRange> PRIME_CODEC = Codec.INT.xmap(FocalRange::new, FocalRange::min);

    public static final Codec<FocalRange> CODEC = Codec.either(PRIME_CODEC, RANGE_CODEC).xmap(Either::unwrap,
            focalRange -> focalRange.isPrime() ? Either.left(focalRange) : Either.right(focalRange));

    public static final StreamCodec<ByteBuf, FocalRange> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FocalRange::min,
            ByteBufCodecs.VAR_INT, FocalRange::max,
            FocalRange::new
    );

    public static final FocalRange REGULAR = new FocalRange(18, 55);
    public static final int ALLOWED_MIN = 10;
    public static final int ALLOWED_MAX = 300;

    private final int min;
    private final int max;

    public FocalRange(int min, int max) {
        Preconditions.checkArgument(ALLOWED_MIN <= min && min <= ALLOWED_MAX,
                min + " is not in allowed range for 'min'.");
        Preconditions.checkArgument(ALLOWED_MIN <= max && max <= ALLOWED_MAX,
                max + " is not in allowed range for 'max'.");
        Preconditions.checkArgument(min <= max,
                "'min' should not be larger than 'max'. min: " + min + ", max: " + max);
        this.min = min;
        this.max = max;
    }

    public FocalRange(int primeValue) {
        Preconditions.checkArgument(ALLOWED_MIN <= primeValue && primeValue <= ALLOWED_MAX,
                primeValue + " is not in allowed range: " + ALLOWED_MIN + "-" + ALLOWED_MAX);
        this.min = primeValue;
        this.max = primeValue;
    }

    public boolean isPrime() {
        return min == max;
    }

    public float focalLengthFromZoom(float zoom) {
        zoom = Mth.clamp(zoom, 0, 1);
        return Mth.map(zoom, 0, 1, min, max);
    }

    public float fovFromZoom(float zoom) {
        return Fov.focalLengthToFov(focalLengthFromZoom(zoom));
    }

    public double zoomFromFov(double fov) {
        double focalLength = Fov.fovToFocalLength(fov);
        return Mth.map(focalLength, min(), max(), 0.0, 1.0);
    }

    public double clampFocalLength(double focalLength) {
        return Mth.clamp(focalLength, min, max);
    }

    public float clampFov(float fov) {
        return Mth.clamp(fov, Fov.focalLengthToFov(max), Fov.focalLengthToFov(min));
    }

    public static @NotNull FocalRange getDefault() {
        return parse(Config.Server.CAMERA_DEFAULT_FOCAL_RANGE.get());
    }

    @Override
    public @NotNull String getSerializedName() {
        return isPrime() ? Integer.toString(min) : min + "-" + max;
    }

    public static FocalRange parse(String value) {
        int dashIndex = value.indexOf("-");
        if (dashIndex == -1) {
            int prime = Integer.parseInt(value);
            return new FocalRange(prime);
        }

        int min = Integer.parseInt(value.substring(0, dashIndex));
        int max = Integer.parseInt(value.substring(dashIndex + 1));
        return new FocalRange(min, max);
    }

    public static FocalRange fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            throw new JsonSyntaxException("Item cannot be null");

        if (json.isJsonPrimitive()) {
            int fixedValue = json.getAsInt();
            return new FocalRange(fixedValue);
        }

        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            int min = obj.get("min").getAsInt();
            int max = obj.get("max").getAsInt();
            return new FocalRange(min, max);
        }

        throw new JsonSyntaxException("Invalid FocalRange json. Expected a number or json object with 'min' and 'max'.");
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FocalRange) obj;
        return this.min == that.min && this.max == that.max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        if (isPrime())
            return "FocalRange[" + "fixed=" + min + ']';
        else
            return "FocalRange[" + "min=" + min + ", " + "max=" + max + ']';
    }
}
