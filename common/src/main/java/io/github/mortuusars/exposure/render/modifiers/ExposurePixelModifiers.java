package io.github.mortuusars.exposure.render.modifiers;

public class ExposurePixelModifiers {
    public static final IPixelModifier EMPTY = new IPixelModifier() {
        @Override
        public String getIdSuffix() {
            return "";
        }
    };
    public static final IPixelModifier NEGATIVE = new NegativeFilmPixelModifier(false);
    public static final IPixelModifier NEGATIVE_FILM = new NegativeFilmPixelModifier(true);

    // HSB is faster while giving similar results. HSLUV creates noticeable freezes when exposure is loaded.
    public static final IPixelModifier AGED = new AgedHSBPixelModifier(0xD9A863, 0.65f, 40, 255);
}
