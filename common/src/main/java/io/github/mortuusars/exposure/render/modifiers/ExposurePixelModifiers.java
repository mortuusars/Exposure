package io.github.mortuusars.exposure.render.modifiers;

public class ExposurePixelModifiers {
    public static final IPixelModifier EMPTY = new IPixelModifier() {};
    public static final IPixelModifier NEGATIVE = new NegativeFilmPixelModifier(false);
    public static final IPixelModifier NEGATIVE_FILM = new NegativeFilmPixelModifier(true);
    public static final IPixelModifier SEPIA = new SepiaPixelModifier();
}
