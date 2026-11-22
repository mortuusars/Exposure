package io.github.mortuusars.exposure.util;

public class Fov {
    public static double fovToFocalLength(double fov) {
        return fovToFocalLength(fov, 36);
    }

    public static float focalLengthToFov(float focalLength) {
        return focalLengthToFov(focalLength, 36);
    }

    public static double fovToFocalLength(double fov, double filmWidth) {
        return filmWidth / (2.0f * Math.tan(Math.toRadians(fov / 2.0)));
    }

    public static float focalLengthToFov(float focalLength, float filmWidth) {
        return (float) (2.0 * Math.toDegrees(Math.atan(filmWidth / (2.0 * focalLength))));
    }
}
