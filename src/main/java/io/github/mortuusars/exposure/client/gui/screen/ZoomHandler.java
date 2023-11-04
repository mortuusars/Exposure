package io.github.mortuusars.exposure.client.gui.screen;

import io.github.mortuusars.exposure.camera.infrastructure.ZoomDirection;
import net.minecraft.util.Mth;

public class ZoomHandler {
    public float step = 1.35f;
    public float defaultZoom = 0.75f;
    public float targetZoom = defaultZoom;
    public float zoomInSpeed = 0.6f;
    public float zoomOutSpeed = 0.8f;
    public float minZoom = defaultZoom / (float)Math.pow(step, 4f);
    public float maxZoom = defaultZoom * (float)Math.pow(step, 4f);

    private float currentZoom = 0.1f;

    public float get() {
        return currentZoom;
    }

    public void update(float partialTicks) {
        float delta = Math.min((currentZoom < targetZoom ? zoomInSpeed : zoomOutSpeed) * partialTicks, 1f);
        currentZoom = Mth.lerp(delta, currentZoom, targetZoom);
    }

    public void change(ZoomDirection direction) {
        set(direction == ZoomDirection.IN ? targetZoom * step : targetZoom / step);
    }

    public void set(float target) {
        targetZoom = Mth.clamp(target, minZoom, maxZoom);
    }
}
