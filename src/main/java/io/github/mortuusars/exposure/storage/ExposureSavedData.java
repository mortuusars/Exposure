package io.github.mortuusars.exposure.storage;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.zip.*;

public class ExposureSavedData extends SavedData {
    private int width;
    private int height;
    private byte[] pixels;

    public ExposureSavedData(int width, int height) {
        this(width, height, new byte[width * height]);
    }

    public ExposureSavedData(int width, int height, byte[] pixels) {
        Preconditions.checkArgument(width >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(height >= 0, "Height cannot be negative.");

        if (pixels.length > width * height)
            Exposure.LOGGER.warn("Pixel count was larger that it supposed to be. This shouldn't happen.");

        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setPixel(int x, int y, byte value) {
        Preconditions.checkArgument(x >= 0 && x < width,  "X=" + x + " is out of bounds for Width=" + width);
        Preconditions.checkArgument(y >= 0 && y < height,  "Y=" + x + " is out of bounds for Height=" + height);
        pixels[y * width + x] = value;
    }

    public byte getPixel(int x, int y) {
        return pixels[y * width + x];
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {

        byte[] copy = Arrays.copyOf(pixels, pixels.length);
        byte[] compress = compress(copy);


        compoundTag.putInt("width", width);
        compoundTag.putInt("height", height);
        compoundTag.putByteArray("pixels", compress);
        return compoundTag;
    }

    public static ExposureSavedData load(CompoundTag compoundTag) {
        int width = compoundTag.getInt("width");
        int height = compoundTag.getInt("height");
        byte[] pixelsCompressed = compoundTag.getByteArray("pixels");
        byte[] pixels = decompress(pixelsCompressed);

        return new ExposureSavedData(width, height, pixels);
    }

    public static byte[] compress(byte[] in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            defl.write(in);
            defl.flush();
            defl.close();

            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(150);
            return null;
        }
    }

    public static byte[] decompress(byte[] in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InflaterOutputStream infl = new InflaterOutputStream(out);
            infl.write(in);
            infl.flush();
            infl.close();

            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(150);
            return null;
        }
    }

//    public static byte[] compress(byte[] data) {
//        Deflater deflater = new Deflater();
//        deflater.setInput(data);
//        deflater.finish();
//
//        byte[] buffer = new byte[data.length];
//        int compressedLength = deflater.deflate(buffer);
//
//        byte[] compressedData = new byte[compressedLength];
//        System.arraycopy(buffer, 0, compressedData, 0, compressedLength);
//
//        deflater.end();
//
//        return compressedData;
//    }
//
//    public static byte[] decompress(byte[] compressedData) {
//        Inflater inflater = new Inflater();
//        inflater.setInput(compressedData);
//
//        byte[] buffer = new byte[compressedData.length];
//        int decompressedLength;
//        try {
//            decompressedLength = inflater.inflate(buffer);
//        } catch (DataFormatException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        byte[] decompressedData = new byte[decompressedLength];
//        System.arraycopy(buffer, 0, decompressedData, 0, decompressedLength);
//
//        inflater.end();
//
//        return decompressedData;
//    }
}
