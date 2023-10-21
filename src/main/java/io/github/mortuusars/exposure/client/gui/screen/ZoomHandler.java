package io.github.mortuusars.exposure.client.gui.screen;

import net.minecraft.util.Mth;

public class ZoomHandler {
    private float minZoom = 0.25f;
    private float maxZoom = 2f;
    private float defaultZoom = 0.75f;
    private float zoomInSpeed = 0.6f;
    private float zoomOutSpeed = 0.8f;
    private float targetZoom = defaultZoom;
    private float currentZoom = 0.1f;

    public void update(float partialTicks) {
        currentZoom = Mth.lerp(Math.min((currentZoom < targetZoom ? zoomInSpeed : zoomOutSpeed) * partialTicks, 1f), currentZoom, targetZoom);
    }

    public float getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
    }

    public float getDefaultZoom() {
        return defaultZoom;
    }

    public void setDefaultZoom(float defaultZoom) {
        this.defaultZoom = defaultZoom;
    }

    public float getZoomInSpeed() {
        return zoomInSpeed;
    }

    public void setZoomInSpeed(float zoomInSpeed) {
        this.zoomInSpeed = zoomInSpeed;
    }

    public float getZoomOutSpeed() {
        return zoomOutSpeed;
    }

    public void setZoomOutSpeed(float zoomOutSpeed) {
        this.zoomOutSpeed = zoomOutSpeed;
    }

    public float getTargetZoom() {
        return targetZoom;
    }

    public void set(float targetZoom) {
        this.targetZoom = Mth.clamp(targetZoom, minZoom, maxZoom);
    }

    public void add(float zoom) {
        set(getTargetZoom() + zoom);
    }

    public float get() {
        return currentZoom;
    }

    public void setCurrentZoom(float currentZoom) {
        this.currentZoom = currentZoom;
    }
}
