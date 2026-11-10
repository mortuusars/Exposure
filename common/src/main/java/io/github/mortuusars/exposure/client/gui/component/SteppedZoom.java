package io.github.mortuusars.exposure.client.gui.component;

import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.util.ZoomDirection;
import io.github.mortuusars.mortaar.util.Easing;
import net.minecraft.util.Mth;

public class SteppedZoom {
    protected double defaultZoom = 1;
    protected int zoomInSteps = 4;
    protected int zoomOutSteps = 4;
    protected double zoomPerStep = 1.4;
    protected Animation animation = new Animation(300, Easing.OUT_EXPO);

    protected double target;
    protected double current;

    // --

    public void zoom(ZoomDirection direction) {
        if (direction == ZoomDirection.IN) {
            zoomIn();
        } else {
            zoomOut();
        }
    }

    public void zoomIn() {
        setTarget(target * zoomPerStep);
    }

    public void zoomOut() {
        setTarget(target / zoomPerStep);
    }

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.current = get();
        this.target = Mth.clamp(target, getMin(), getMax());
        animation.resetProgress();
    }

    public double get() {
        return Mth.lerp(animation.getValue(), current, target);
    }

    public double getMin() {
        return defaultZoom / Math.pow(zoomPerStep, zoomOutSteps);
    }

    public double getMax() {
        return defaultZoom * Math.pow(zoomPerStep, zoomInSteps);
    }

    // --

    public double getDefaultZoom() {
        return defaultZoom;
    }

    public SteppedZoom defaultZoom(double defaultZoom) {
        this.defaultZoom = defaultZoom;
        setTarget(defaultZoom);
        return this;
    }

    public int getZoomInSteps() {
        return zoomInSteps;
    }

    public SteppedZoom zoomInSteps(int zoomInSteps) {
        this.zoomInSteps = zoomInSteps;
        return this;
    }

    public int getZoomOutSteps() {
        return zoomOutSteps;
    }

    public SteppedZoom zoomOutSteps(int zoomOutSteps) {
        this.zoomOutSteps = zoomOutSteps;
        return this;
    }

    public double getZoomPerStep() {
        return zoomPerStep;
    }

    public SteppedZoom zoomPerStep(double zoomPerStep) {
        this.zoomPerStep = zoomPerStep;
        return this;
    }

    public Animation getAnimation() {
        return animation;
    }

    public SteppedZoom animation(Animation animation) {
        this.animation = animation;
        return this;
    }
}
