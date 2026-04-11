package io.github.mortuusars.exposure.world.level.storage;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.util.Codecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class ExposureData extends SavedData {
    public static final Codec<ExposureData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("width").forGetter(ExposureData::getWidth),
            Codec.INT.fieldOf("height").forGetter(ExposureData::getHeight),
            Codecs.byteArrayCodec(1, 2048 * 2048).fieldOf("pixels").forGetter(ExposureData::getPixels),
            Identifier.CODEC.optionalFieldOf("palette", ColorPalettes.DEFAULT.identifier()).forGetter(ExposureData::getPaletteId),
            Tag.CODEC.optionalFieldOf("tag", Tag.EMPTY).forGetter(ExposureData::getTag)
    ).apply(instance, ExposureData::new));

    public static final StreamCodec<ByteBuf, ExposureData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ExposureData::getWidth,
            ByteBufCodecs.VAR_INT, ExposureData::getHeight,
            ByteBufCodecs.byteArray(2048 * 2048), ExposureData::getPixels,
            Identifier.STREAM_CODEC, ExposureData::getPaletteId,
            Tag.STREAM_CODEC, ExposureData::getTag,
            ExposureData::new
    );

    public static final ExposureData EMPTY = new ExposureData(
            1, 1, new byte[]{0}, ColorPalettes.DEFAULT.identifier(), Tag.EMPTY);

    private final int width;
    private final int height;
    private final byte[] pixels;
    private final Identifier palette;
    private final Tag tag;

    public ExposureData(int width, int height, byte[] pixels, Identifier paletteId, Tag tag) {
        Preconditions.checkArgument(width > 0, "Width should be larger than 0. %s", width);
        Preconditions.checkArgument(height > 0, "Height should be larger than 0. %s ", height);
        Preconditions.checkArgument(pixels.length == width * height,
                "Pixel count '%s' is not correct for image dimensions of '%sx%s'. " +
                        "Count should be '%s'.", pixels.length, width, height, width * height);
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.palette = paletteId;
        this.tag = tag;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getPixels() {
        return pixels;
    }

    public byte getPixel(int x, int y) {
        return pixels[y * width + x];
    }

    public Identifier getPaletteId() {
        return palette;
    }

    public Tag getTag() {
        return tag;
    }

    public ExposureData withTag(Function<Tag, Tag> updateFunction) {
        return new ExposureData(width, height, pixels, palette, updateFunction.apply(this.tag));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExposureData that = (ExposureData) o;
        return width == that.width && height == that.height && Objects.deepEquals(pixels, that.pixels)
                && Objects.equals(palette, that.palette) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, Arrays.hashCode(pixels), palette, tag);
    }

    // --

    public static SavedDataType<ExposureData> factory(String path) {
        return new SavedDataType<>(path, () -> {
            throw new IllegalStateException("Should never create an empty exposure saved data");
        }, CODEC, null);
    }

//    public static ExposureData load(ValueInput tag) {
//        return CODEC.decode(tag).getOrThrow().getFirst();
//    }

    public record Tag(ExposureType type,
                      String creator,
                      long unixTimestamp,
                      boolean loaded,
                      boolean wasPrinted) {
        public static final Codec<Tag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(Tag::type),
                Codec.STRING.optionalFieldOf("creator", "").forGetter(Tag::creator),
                Codec.LONG.optionalFieldOf("timestamp", 1645494000L).forGetter(Tag::unixTimestamp),
                Codec.BOOL.optionalFieldOf("loaded", false).forGetter(Tag::loaded),
                Codec.BOOL.optionalFieldOf("was_printed", false).forGetter(Tag::wasPrinted)
        ).apply(instance, Tag::new));

        public static final StreamCodec<ByteBuf, Tag> STREAM_CODEC = new StreamCodec<>() {
            public @NotNull ExposureData.Tag decode(ByteBuf buffer) {
                return new Tag(
                        ExposureType.STREAM_CODEC.decode(buffer),
                        ByteBufCodecs.STRING_UTF8.decode(buffer),
                        ByteBufCodecs.VAR_LONG.decode(buffer),
                        ByteBufCodecs.BOOL.decode(buffer),
                        ByteBufCodecs.BOOL.decode(buffer));
            }

            public void encode(ByteBuf buffer, Tag data) {
                ExposureType.STREAM_CODEC.encode(buffer, data.type());
                ByteBufCodecs.STRING_UTF8.encode(buffer, data.creator());
                ByteBufCodecs.VAR_LONG.encode(buffer, data.unixTimestamp());
                ByteBufCodecs.BOOL.encode(buffer, data.loaded());
                ByteBufCodecs.BOOL.encode(buffer, data.wasPrinted());
            }
        };

        public static final Tag EMPTY = new Tag(ExposureType.COLOR, "", 0, false, false);

        public Tag withType(ExposureType type) {
            return new Tag(type, creator, unixTimestamp, loaded, wasPrinted);
        }

        public Tag withCreator(String creator) {
            return new Tag(type, creator, unixTimestamp, loaded, wasPrinted);
        }

        public Tag withTimestamp(long unixTimestamp) {
            return new Tag(type, creator, unixTimestamp, loaded, wasPrinted);
        }

        public Tag withLoadedSetTo(boolean isLoaded) {
            return new Tag(type, creator, unixTimestamp, isLoaded, wasPrinted);
        }

        public Tag withWasPrintedSetTo(boolean wasPrinted) {
            return new Tag(type, creator, unixTimestamp, loaded, wasPrinted);
        }

        public Tag setPrinted() {
            return new Tag(type, creator, unixTimestamp, loaded, true);
        }
    }
}
