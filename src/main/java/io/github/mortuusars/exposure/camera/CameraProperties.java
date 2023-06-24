package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.film.FilmType;
import net.minecraft.advancements.critereon.MinMaxBounds;

public class CameraProperties {
    private final int filmSize;
    private final FilmType filmType;
    private final MinMaxBounds<Integer> focalRange;
    private final float shutterSpeed;
    private final boolean flash;

    public CameraProperties(int filmSize, FilmType filmType, MinMaxBounds<Integer> focalRange, float shutterSpeed, boolean flash) {
        this.filmSize = filmSize;
        this.filmType = filmType;
        this.focalRange = focalRange;
        this.shutterSpeed = shutterSpeed;
        this.flash = flash;
    }

    public int getFilmSize() {
        return filmSize;
    }

    public FilmType getFilmType() {
        return filmType;
    }

    public MinMaxBounds<Integer> getFocalRange() {
        return focalRange;
    }

    public float getShutterSpeed() {
        return shutterSpeed;
    }

    public boolean isFlash() {
        return flash;
    }
}
