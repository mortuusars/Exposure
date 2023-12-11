package io.github.mortuusars.exposure.camera.infrastructure;

public record FocalRange(float min, float max) {
    public static final FocalRange FULL = new FocalRange(18, 200);
    public static final FocalRange SHORT = new FocalRange(18, 55);
    public static final FocalRange LONG = new FocalRange(55, 200);
}
