package io.github.mortuusars.exposure.data.storage;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class ExposureSavedData extends SavedData {
    public static final String TYPE_PROPERTY = "Type";
    public static final String WAS_PRINTED_PROPERTY = "WasPrinted";

    private final int width;
    private final int height;
    private final byte[] pixels;
    private final CompoundTag properties;

    public ExposureSavedData(int width, int height, byte[] pixels, CompoundTag properties) {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(height >= 0, "Height cannot be negative.");

        if (pixels.length > width * height)
            LogUtils.getLogger().warn("Pixel count was larger that it supposed to be. This shouldn't happen.");

        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.properties = properties;
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

    public void setPixel(int x, int y, byte value) {
        Preconditions.checkArgument(x >= 0 && x < width,  "X=" + x + " is out of bounds for Width=" + width);
        Preconditions.checkArgument(y >= 0 && y < height,  "Y=" + x + " is out of bounds for Height=" + height);
        pixels[y * width + x] = value;
    }

    public CompoundTag getProperties() {
        return properties;
    }

    public FilmType getType() {
        String typeString = properties.getString(TYPE_PROPERTY);
        return FilmType.byName(typeString);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("width", width);
        compoundTag.putInt("height", height);
        compoundTag.putByteArray("pixels", pixels);
        compoundTag.put("properties", properties);
        return compoundTag;
    }

    public static ExposureSavedData load(CompoundTag compoundTag) {
        CompoundTag properties = compoundTag.getCompound("properties");

        // Backwards compatibility:
        if (!properties.contains(TYPE_PROPERTY)) {
            properties.putString(TYPE_PROPERTY, compoundTag.getBoolean("black_and_white") ? "black_and_white" : "color");
        }
        if (!properties.contains(WAS_PRINTED_PROPERTY)) {
            properties.putBoolean(WAS_PRINTED_PROPERTY, compoundTag.getBoolean("printed"));
        }

        return new ExposureSavedData(
                compoundTag.getInt("width"),
                compoundTag.getInt("height"),
                compoundTag.getByteArray("pixels"),
                properties);
    }
}
