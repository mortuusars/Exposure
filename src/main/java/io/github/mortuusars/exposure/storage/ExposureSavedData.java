package io.github.mortuusars.exposure.storage;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.film.FilmType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class ExposureSavedData extends SavedData {
    private final int width;
    private final int height;
    private final byte[] pixels;
    private final FilmType type;
    private boolean printed;

    public ExposureSavedData(int width, int height) {
        this(width, height, new byte[width * height]);
    }

    public ExposureSavedData(int width, int height, byte[] pixels) {
        this(width, height, pixels, false);
    }

    public ExposureSavedData(int width, int height, byte[] pixels, boolean printed) {
        this(width, height, pixels, FilmType.COLOR, printed);
    }

    public ExposureSavedData(int width, int height, byte[] pixels, FilmType type, boolean printed) {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(height >= 0, "Height cannot be negative.");

        if (pixels.length > width * height)
            Exposure.LOGGER.warn("Pixel count was larger that it supposed to be. This shouldn't happen.");

        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.type = type;
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

    public FilmType getType() {
        return type;
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
        if (type == FilmType.BLACK_AND_WHITE)
            compoundTag.putBoolean("black_and_white", true);
        if (printed)
            compoundTag.putBoolean("printed", true);
        return compoundTag;
    }

    public static ExposureSavedData load(CompoundTag compoundTag) {
        return new ExposureSavedData(
                compoundTag.getInt("width"),
                compoundTag.getInt("height"),
                compoundTag.getByteArray("pixels"),
                compoundTag.getBoolean("black_and_white") ? FilmType.BLACK_AND_WHITE : FilmType.COLOR,
                compoundTag.getBoolean("printed"));
    }
}
