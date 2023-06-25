package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.viewfinder.IViewfinderNew;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderNew;

public class Camera {
    public record FocalRange(float min, float max) {
        public static final FocalRange DEFAULT = new FocalRange(18, 200);
    }

    private static IViewfinderNew viewfinder = new ViewfinderNew();

    public static IViewfinderNew getViewfinder() {
        return viewfinder;
    }
}
