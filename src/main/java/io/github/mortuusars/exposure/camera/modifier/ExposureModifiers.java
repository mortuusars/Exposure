package io.github.mortuusars.exposure.camera.modifier;

import io.github.mortuusars.exposure.camera.IExposureModifier;
import io.github.mortuusars.exposure.camera.component.ShutterSpeed;

public class ExposureModifiers {
    public static final IExposureModifier BRIGHTNESS = new BrightnessModifier("brightness", new ShutterSpeed(1/60f, 1));
    public static final IExposureModifier BLACK_AND_WHITE = new BlackAndWhiteModifier("black_and_white");
}
