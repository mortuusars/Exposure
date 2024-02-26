package io.github.mortuusars.exposure.render.modifiers;

public interface IPixelModifier {
    /**
     * Suffix is used to differentiate between cached rendered exposures.
     */
    String getIdSuffix();
    default int modifyPixel(int ABGR) {
        return ABGR;
    };
}
