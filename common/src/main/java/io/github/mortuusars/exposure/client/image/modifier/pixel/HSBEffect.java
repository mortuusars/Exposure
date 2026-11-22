package io.github.mortuusars.exposure.client.image.modifier.pixel;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class HSBEffect implements PixelEffect {
    protected final float h, s, b;

    public HSBEffect(float h, float s, float b) {
        Preconditions.checkArgument(h >= -1 && h <= 1, "h must be in -1 to 1 range.");
        Preconditions.checkArgument(s >= -1 && s <= 1, "s must be in -1 to 1 range.");
        Preconditions.checkArgument(b >= -1 && b <= 1, "b must be in -1 to 1 range.");
        this.h = h;
        this.s = s;
        this.b = b;
    }

    @Override
    public String getIdentifier() {
        return "hsb-h%s-s%s-b%s".formatted(h, s, b);
    }

    public int modify(int colorARGB) {
        int alpha = ARGB.alpha(colorARGB);
        int red = ARGB.red(colorARGB);
        int green = ARGB.green(colorARGB);
        int blue = ARGB.blue(colorARGB);

        float[] hsb = Color.HSB.RGBtoHSB(red, green, blue);

        hsb[0] = (hsb[0] + h) % 1.0f;
        if (hsb[0] < 0) hsb[0] += 1.0f;
        hsb[1] = Mth.clamp(hsb[1] * (s + 1), 0, 1);
        hsb[2] = Mth.clamp(hsb[2] * (b + 1), 0, 1);

        return (alpha << 24) | Color.HSB.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }
}
