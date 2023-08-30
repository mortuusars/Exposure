package io.github.mortuusars.exposure.client;

public class GammaModifier {
    private static float additionalBrightness = 0f;

    public static float getAdditionalBrightness() {
        return additionalBrightness;
    }

    public static void setAdditionalBrightness(float additionalBrightness) {
        GammaModifier.additionalBrightness = additionalBrightness;
    }

    public static float modifyBrightness(float originalBrightness) {
        return originalBrightness + additionalBrightness;
    }
}
