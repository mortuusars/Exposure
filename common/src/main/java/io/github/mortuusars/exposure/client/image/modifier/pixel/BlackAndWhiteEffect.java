package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class BlackAndWhiteEffect implements PixelEffect {
    protected final float rWeight;
    protected final float gWeight;
    protected final float bWeight;
    protected String identifier;

    public BlackAndWhiteEffect(float rWeight, float gWeight, float bWeight) {
        this.rWeight = rWeight;
        this.gWeight = gWeight;
        this.bWeight = bWeight;
        this.identifier = "bw-" + rWeight + "-" + gWeight + "-" + bWeight;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public int modify(int colorARGB) {
        int alpha = ARGB.alpha(colorARGB);
        int red = ARGB.red(colorARGB);
        int green = ARGB.green(colorARGB);
        int blue = ARGB.blue(colorARGB);

        int value = Mth.clamp((int) (rWeight * red + gWeight * green + bWeight * blue), 0, 255);
        return ARGB.color(alpha, value, value, value);
    }

    @Override
    public String toString() {
        return "BlackAndWhiteProcessor{weights:%s,%s,%s}".formatted(rWeight, gWeight, bWeight);
    }
}
