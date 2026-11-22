package io.github.mortuusars.exposure.client.image.modifier.pixel;

import com.google.common.base.Preconditions;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class ColorBalanceEffect implements PixelEffect {
    protected final float r, g, b;

    /**
     * 0 means no change.
     */
    public ColorBalanceEffect(float r, float g, float b) {
        Preconditions.checkArgument(r >= -1 && r <= 1, "r must be in -1 to 1 range.");
        Preconditions.checkArgument(g >= -1 && g <= 1, "g must be in -1 to 1 range.");
        Preconditions.checkArgument(b >= -1 && b <= 1, "b must be in -1 to 1 range.");
        // for some reason r and b have to be swapped
        this.r = b;
        this.g = g;
        this.b = r;
    }

    @Override
    public String getIdentifier() {
        return "color-balance-r%s-g%s-b%s".formatted(r, g, b);
    }

    public int modify(int colorARGB) {
        if (r == 0f && g == 0f && b == 0f) return colorARGB;

        int alpha = ARGB.alpha(colorARGB);
        int red = ARGB.red(colorARGB);
        int green = ARGB.green(colorARGB);
        int blue = ARGB.blue(colorARGB);

        red = Mth.clamp(Mth.floor(red * (r + 1)), 0, 255);
        green = Mth.clamp(Mth.floor(green * (g + 1)), 0, 255);
        blue = Mth.clamp(Mth.floor(blue * (b + 1)), 0, 255);

        return ARGB.color(alpha, red, green, blue);
    }

    @Override
    public String toString() {
        return "ColorBalance{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                '}';
    }
}
