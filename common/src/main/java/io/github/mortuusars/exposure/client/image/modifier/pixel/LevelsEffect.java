package io.github.mortuusars.exposure.client.image.modifier.pixel;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.world.camera.film.properties.Levels;
import net.minecraft.util.ARGB;

public class LevelsEffect implements PixelEffect {
    protected final int shadows;
    protected final int midtones;
    protected final int highlights;
    protected final int black;
    protected final int white;

    public LevelsEffect(int shadows, int midtones, int highlights, int black, int white) {
        checkRange(shadows, "shadows");
        checkRange(midtones, "midtones");
        checkRange(highlights, "highlights");
        checkRange(black, "black");
        checkRange(white, "white");
        this.shadows = shadows;
        this.midtones = midtones;
        this.highlights = highlights;
        this.black = black;
        this.white = white;
    }

    public LevelsEffect(Levels levels) {
        this(levels.shadows(), levels.midtones(), levels.highlights(), levels.black(), levels.white());
    }

    private static void checkRange(int value, String name) {
        Preconditions.checkArgument(value >= 0 && value <= 255, name + " '" + value + "' is not valid. 0-255.");
    }

    @Override
    public String getIdentifier() {
        return "levels-s%s-m%s-h%s-b%s-w%s".formatted(shadows, midtones, highlights, black, white);
    }

    public int modify(int colorARGB) {
        int alpha = ARGB.alpha(colorARGB);
        int red = ARGB.red(colorARGB);
        int green = ARGB.green(colorARGB);
        int blue = ARGB.blue(colorARGB);

        double gammaCorrection = calculateGammaCorrection(midtones);

        red = adjustChannel(red, shadows, highlights, black, white, gammaCorrection);
        green = adjustChannel(green, shadows, highlights, black, white, gammaCorrection);
        blue = adjustChannel(blue, shadows, highlights, black, white, gammaCorrection);

        return ARGB.color(alpha, red, green, blue);
    }

    protected double calculateGammaCorrection(int inputMid) {
        double midtoneNormal = inputMid / 255.0;
        double gamma = 1.0;

        if (inputMid < 128) {
            midtoneNormal *= 2;
            gamma = 1 + (9 * (1 - midtoneNormal));
            gamma = Math.min(gamma, 9.99);
        } else if (inputMid > 128) {
            midtoneNormal = (midtoneNormal * 2) - 1;
            gamma = 1 - midtoneNormal;
            gamma = Math.max(gamma, 0.01);
        }

        return 1.0 / gamma;
    }

    protected int adjustChannel(int value, int inputBlack, int inputWhite, int outputBlack, int outputWhite, double gammaCorrection) {
        value = (int) (255.0 * (value - inputBlack) / (inputWhite - inputBlack));
        value = Math.max(0, Math.min(255, value));

        if (gammaCorrection != 1.0) {
            value = (int) (255.0 * Math.pow(value / 255.0, gammaCorrection));
        }

        value = (int) ((value / 255.0) * (outputWhite - outputBlack) + outputBlack);
        return Math.max(0, Math.min(255, value));
    }
}
