package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.viewfinder.IViewfinder;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;

public class Camera {
    public record FocalRange(float min, float max) {
        public static final FocalRange DEFAULT = new FocalRange(18, 200);
    }

    private static IViewfinder viewfinder = new Viewfinder();

    public static IViewfinder getViewfinder() {
        return viewfinder;
    }
}
