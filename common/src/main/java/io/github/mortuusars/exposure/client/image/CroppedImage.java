package io.github.mortuusars.exposure.client.image;

import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.Rect2i;

public class CroppedImage implements Image {
    private final Image original;
    private final Rect2i crop;

    public CroppedImage(Image original, Rect2i crop) {
        Preconditions.checkArgument(crop.getX() >= 0 && crop.getY() >= 0
                        && crop.getX() + crop.getWidth() <= original.width()
                        && crop.getY() + crop.getHeight() <= original.height(),
                "%s is out of bounds for image size {%s, %s}", crop, original.width(), original.height());
        this.original = original;
        this.crop = crop;
    }

    @Override
    public int width() {
        return crop.getWidth();
    }

    @Override
    public int height() {
        return crop.getHeight();
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return original.getPixelARGB(crop.getX() + x, crop.getY() + y);
    }

    @Override
    public void close() {
        original.close();
    }
}
