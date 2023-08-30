package io.github.mortuusars.exposure.camera.capture;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.ItemAndStack;

@SuppressWarnings("ClassCanBeRecord")
public class CaptureProperties {
    public final ItemAndStack<CameraItem> camera;
    public final int filmSize;
    public final float brightnessStops;
    public final boolean flash;

    public CaptureProperties(ItemAndStack<CameraItem> camera, int filmSize, float brightnessStops, boolean flash) {
        this.camera = camera;
        this.filmSize = filmSize;
        this.brightnessStops = brightnessStops;
        this.flash = flash;
    }
}
