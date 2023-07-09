package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.camera.modifier.IExposureModifier;
import io.github.mortuusars.exposure.util.CameraInHand;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class Capture {
    public final CameraInHand camera;
    public final String id;
    public final int size;
    public final float cropFactor;
    public final ShutterSpeed shutterSpeed;
    public final List<IExposureModifier> modifiers;

    public Capture(CameraInHand camera, String id, int size, float cropFactor, ShutterSpeed shutterSpeed, List<IExposureModifier> modifiers) {
        this.camera = camera;
        this.id = id;
        this.size = size;
        this.cropFactor = cropFactor;
        this.shutterSpeed = shutterSpeed;
        this.modifiers = modifiers;
    }
}
