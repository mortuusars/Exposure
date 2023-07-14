package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.modifier.IExposureModifier;
import io.github.mortuusars.exposure.storage.saver.IExposureSaver;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class Capture {
    public final String id;
    public final int size;
    public final float cropFactor;
    public final float brightnessStops;
    public final List<IExposureModifier> modifiers;
    public final List<IExposureSaver> savers;

    public Capture(String id, int size, float cropFactor, float brightnessStops, List<IExposureModifier> modifiers, List<IExposureSaver> savers) {
        this.id = id;
        this.size = size;
        this.cropFactor = cropFactor;
        this.brightnessStops = brightnessStops;
        this.modifiers = modifiers;
        this.savers = savers;
    }
}
