package io.github.mortuusars.exposure.client.image;

import com.google.common.base.Preconditions;
import net.minecraft.util.ARGB;

public class TrichromeImage implements Image {
    private final Image red;
    private final Image green;
    private final Image blue;
    private final int width, height;

    public TrichromeImage(Image red, Image green, Image blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.width = Math.min(red.width(), Math.min(green.width(), blue.width()));
        this.height = Math.min(red.height(), Math.min(green.height(), blue.height()));
        Preconditions.checkArgument(this.width > 0,
                "Cannot create TrichromeImage: " +
                        "smallest image should have width larger than 0. {%s, %s, %s}", red, green, blue);
        Preconditions.checkArgument(this.height > 0,
                "Cannot create TrichromeImage: " +
                        "smallest image should have height larger than 0. {%s, %s, %s}", red, green, blue);
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return ARGB.color(
                ARGB.alpha(red.getPixelARGB(x, y)),
                ARGB.red(red.getPixelARGB(x, y)),
                ARGB.green(green.getPixelARGB(x, y)),
                ARGB.blue(blue.getPixelARGB(x, y)));
    }

    public static TrichromeImage withSize(Image red, Image green, Image blue, int width, int height) {
        return new TrichromeImage(new ResizedImage(red, width, height),
                new ResizedImage(green, width, height),
                new ResizedImage(blue, width, height));
    }
}
