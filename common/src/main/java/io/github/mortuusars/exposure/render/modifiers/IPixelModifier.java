package io.github.mortuusars.exposure.render.modifiers;

public interface IPixelModifier {
    default int modifyPixel(int ABGR) {
        return ABGR;
    };

    default String getIdSuffix() {
        return "";
    }
}
