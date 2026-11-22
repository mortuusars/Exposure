package io.github.mortuusars.exposure.client.image.modifier.pixel;

import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class ExposureEffect implements PixelEffect {
    protected final float brightness;

    /**
     * @param brightness 1 means no change.
     */
    public ExposureEffect(float brightness) {
        this.brightness = brightness;
    }

    public ExposureEffect(float brightnessStops, float brightnessPerStop) {
        this(1f + brightnessStops * brightnessPerStop);
    }

    public ExposureEffect(ShutterSpeed shutterSpeed) {
        this(shutterSpeed.getStops(), 0.2f);
    }

    @Override
    public String getIdentifier() {
        return "brightness-" + brightness;
    }

    public int modify(int colorARGB) {
        if (brightness == 1f) return colorARGB;

        int alpha = ARGB.alpha(colorARGB);
        int red = ARGB.red(colorARGB);
        int green = ARGB.green(colorARGB);
        int blue = ARGB.blue(colorARGB);

        // We simulate bright light by not modifying all pixels equally
        float lightness = (blue + green + red) / 765f; // from 0.0 to 1.0
        float bias;
        if (brightness < 1)
            bias = (1f - lightness) * 0.8f + 0.2f;
        else {
            float curve = (float) Math.pow(Math.sin(lightness * Math.PI), 2);
            bias = lightness > 0.5f ? curve * 0.8f + 0.2f : curve * 0.5f + 0.5f;
        }

        float b = Mth.lerp(bias, blue, blue * brightness);
        float g = Mth.lerp(bias, green, green * brightness);
        float r = Mth.lerp(bias, red, red * brightness);

        // Above values are not clamped at 255 purposely.
        // Excess is redistributed to other channels. As a result - color gets less saturated, which gives more natural color.
        int[] rdst = redistribute(r, g, b);

        // BUT it does not look perfect (IDK, maybe because of dithering), so we blend them together.
        // This makes transitions smoother, subtler. Which looks good imo.
        return ARGB.color(alpha,
                Mth.clamp(Mth.lerpInt(0.5f, (int)r, rdst[0]), 0, 255),
                Mth.clamp(Mth.lerpInt(0.5f, (int)g, rdst[1]), 0, 255),
                Mth.clamp(Mth.lerpInt(0.5f, (int)b, rdst[2]), 0, 255));
    }

    /**
     * Redistributes excess (> 255) values to other channels.
     * Adapted from Mark Ransom's answer:
     * <a href="https://stackoverflow.com/a/141943">StackOverflow</a>
     */
    private int[] redistribute(float red, float green, float blue) {
        float threshold = 255.999f;
        float max = Math.max(red, Math.max(green, blue));
        if (max <= threshold) {
            return new int[]{
                    Mth.clamp(Math.round(red), 0, 255),
                    Mth.clamp(Math.round(green), 0, 255),
                    Mth.clamp(Math.round(blue), 0, 255)};
        }

        float total = red + green + blue;

        if (total >= 3 * threshold)
            return new int[]{(int) threshold, (int) threshold, (int) threshold};

        float x = (3f * threshold - total) / (3f * max - total);
        float gray = threshold - x * max;
        return new int[]{
                Mth.clamp(Math.round(gray + x * red), 0, 255),
                Mth.clamp(Math.round(gray + x * green), 0, 255),
                Mth.clamp(Math.round(gray + x * blue), 0, 255)};
    }

    @Override
    public String toString() {
        return "BrightnessProcessor[" +
                "brightness=" + brightness + ']';
    }
}
