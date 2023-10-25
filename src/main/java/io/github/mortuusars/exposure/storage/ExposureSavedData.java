package io.github.mortuusars.exposure.storage;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class ExposureSavedData extends SavedData {
    private int width;
    private int height;
    private byte[] pixels;
    private boolean printed;

    public ExposureSavedData(int width, int height) {
        this(width, height, new byte[width * height]);
    }

    public ExposureSavedData(int width, int height, byte[] pixels) {
        this(width, height, pixels, false);
    }

    public ExposureSavedData(int width, int height, byte[] pixels, boolean printed) {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(height >= 0, "Height cannot be negative.");

        if (pixels.length > width * height)
            Exposure.LOGGER.warn("Pixel count was larger that it supposed to be. This shouldn't happen.");

        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.printed = printed;
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

    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("width", width);
        compoundTag.putInt("height", height);
        compoundTag.putByteArray("pixels", pixels);
        if (printed)
            compoundTag.putBoolean("printed", true);
        return compoundTag;
    }

    public static ExposureSavedData load(CompoundTag compoundTag) {
        return new ExposureSavedData(
                compoundTag.getInt("width"),
                compoundTag.getInt("height"),
                compoundTag.getByteArray("pixels"),
                compoundTag.getBoolean("printed"));
    }
}
