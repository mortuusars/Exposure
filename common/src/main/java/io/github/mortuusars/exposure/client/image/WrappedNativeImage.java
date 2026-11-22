package io.github.mortuusars.exposure.client.image;

import com.mojang.blaze3d.platform.NativeImage;

public class WrappedNativeImage implements Image {
    private final NativeImage nativeImage;

    public WrappedNativeImage(NativeImage nativeImage) {
        this.nativeImage = nativeImage;
    }

    @Override
    public int width() {
        return nativeImage.getWidth();
    }

    @Override
    public int height() {
        return nativeImage.getHeight();
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return nativeImage.getPixel(x, y);
    }

    @Override
    public void close() {
        nativeImage.close();
    }
}
