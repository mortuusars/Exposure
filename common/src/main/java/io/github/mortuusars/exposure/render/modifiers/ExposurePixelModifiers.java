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
    public static final IPixelModifier AGED = new AgedPixelModifier(0xD9A863, 0.65f, 40, 255);
}
