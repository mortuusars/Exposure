package io.github.mortuusars.exposure.util;

public class Fov {
    public static float fovToFocalLength(float fov) {
        return fovToFocalLength(fov, 36);
    }

    public static float focalLengthToFov(float focalLength) {
        return focalLengthToFov(focalLength, 36);
    }

    public static float fovToFocalLength(float fov, float filmWidth) {
        return (float) (filmWidth / (2.0f * Math.tan(Math.toRadians(fov / 2.0f))));
    }

    public static float focalLengthToFov(float focalLength, float filmWidth) {
        return (float) (2.0 * Math.toDegrees(Math.atan(filmWidth / (2.0 * focalLength))));
    }
}
