package io.github.mortuusars.exposure.camera;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class CaptureProperties {
    public final String id;
    public final int size;
    public final float cropFactor;
    public final float shutterSpeed;
    public final List<IExposureModifier> modifiers;

    public CaptureProperties(String id, int size, float cropFactor, float shutterSpeed, List<IExposureModifier> modifiers) {
        this.id = id;
        this.size = size;
        this.cropFactor = cropFactor;
        this.shutterSpeed = shutterSpeed;
        this.modifiers = modifiers;
    }
}
