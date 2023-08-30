package io.github.mortuusars.exposure.camera.capture.component;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.client.GammaModifier;
import net.minecraft.util.Mth;

import java.awt.*;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class BrightnessComponent implements ICaptureComponent {
    private final float brightnessStops;

    public BrightnessComponent(float brightnessStops) {
        this.brightnessStops = brightnessStops;
    }

    public float getBrightnessStops() {
        return brightnessStops;
    }

    @Override
    public int getFramesDelay(Capture capture) {
        // Changing the gamma is not applied instantly for some reason. Delay of 1 seem to fix it.
        return brightnessStops > 0 ? 1 : 0;
    }

    @Override
    public void setupFrames(Capture capture, int delayFramesLeft) {
        if (brightnessStops >= 0.89f && delayFramesLeft <= 1)
            GammaModifier.setAdditionalBrightness(0.0075f * brightnessStops);
    }

    @Override
    public Color modifyPixel(Capture capture, int red, int green, int blue) {
        float stopsDif = brightnessStops;
        if (stopsDif == 0f)
            return new Color(red, green, blue);

        float brightness = 1f + (stopsDif * (stopsDif < 0 ? 0.2f : 0.35f));

        // We simulate the bright light by not modifying all pixels equally.
        // Bright parts modified more when overexposed and less if underexposed.
        // (the effect is slightly stronger when underexposing)
        float lightness = (red + green + blue) / 765f; // from 0.0 to 1.0
        float bias = stopsDif < 0 ? (1f - lightness) * 0.6f + 0.4f : lightness * 0.4f + 0.6f;

        float r = Mth.lerp(bias, red, red * brightness);
        float g = Mth.lerp(bias, green, green * brightness);
        float b = Mth.lerp(bias, blue, blue * brightness);

        // Above values is not clamped at 255 purposely.
        // Excess is redistributed to other channels. As a result - color gets less saturated, which gives more natural color.
        int[] rdst = redistribute(r, g, b);

        // BUT, it does not look perfect (idk, maybe because of dithering), so we blend them together.
        // This makes transitions smoother, subtler. Which looks good imo.
        return new Color(
                Mth.clamp((int) ((r + rdst[0]) / 2), 0, 255),
                Mth.clamp((int) ((g + rdst[1]) / 2), 0, 255),
                Mth.clamp((int) ((b + rdst[2]) / 2), 0, 255));
    }

    @Override
    public void screenshotTaken(Capture capture, NativeImage screenshot) {
        GammaModifier.setAdditionalBrightness(0f);
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
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BrightnessComponent) obj;
        return Float.floatToIntBits(this.brightnessStops) == Float.floatToIntBits(that.brightnessStops);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brightnessStops);
    }

    @Override
    public String toString() {
        return "BrightnessModifier[" +
                "brightnessStops=" + brightnessStops + ']';
    }

}
