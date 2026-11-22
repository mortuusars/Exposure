package io.github.mortuusars.exposure.client.image.modifier.pixel;

import com.google.common.base.Preconditions;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.Random;

public class NoiseEffect implements PixelEffect {
    protected final Random random = new Random();
    protected final float intensity;

    public NoiseEffect(float intensity) {
        Preconditions.checkArgument(intensity >= 0 && intensity <= 1, "intensity should be in 0-1 range.");
        this.intensity = intensity;
    }

    @Override
    public String getIdentifier() {
        return "noise-" + intensity;
    }

    public int modify(int colorARGB) {
        int alpha = ARGB.alpha(colorARGB);
        int red = ARGB.red(colorARGB);
        int green = ARGB.green(colorARGB);
        int blue = ARGB.blue(colorARGB);

        float brightness = (0.299f * red + 0.587f * green + 0.114f * blue) / 255f;

        // Less noise in bright areas
        float intensity = this.intensity * (1f - (brightness));
        intensity = Mth.lerp(0.25f, intensity, this.intensity);

        int noise = (int) (random.nextGaussian() * intensity * 155);
        int rNoise = (int) (random.nextGaussian() * intensity * 100);
        int gNoise = (int) (random.nextGaussian() * intensity * 100);
        int bNoise = (int) (random.nextGaussian() * intensity * 100);

        red = Mth.clamp(red + noise + rNoise, 0, 255);
        green = Mth.clamp(green + noise + gNoise, 0, 255);
        blue = Mth.clamp(blue + noise + bNoise, 0, 255);

        return ARGB.color(alpha, red, green, blue);
    }
}
