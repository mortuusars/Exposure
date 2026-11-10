package io.github.mortuusars.exposure.client.camera.viewfinder;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import io.github.mortuusars.mortaar.util.Easing;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;

public class ViewfinderSelfie {
    protected final Camera camera;
    protected final Viewfinder viewfinder;

    protected double rawXRot, rawYRot, xRot, yRot, prevXRot, prevYRot;
    protected Animation xRotAnimation = new Animation(200, Easing.OUT_EXPO);
    protected Animation yRotAnimation = new Animation(200, Easing.OUT_EXPO);

    public ViewfinderSelfie(Camera camera, Viewfinder viewfinder) {
        this.camera = camera;
        this.viewfinder = viewfinder;
    }

    public void tick() {
        updateSelfieMode();
    }

    public float getMaxCameraDistance() {
        return Config.Server.SELFIE_CAMERA_DISTANCE.get().floatValue();
    }

    public double getCameraXRot() {
        return Mth.lerp(xRotAnimation.getValue(), prevXRot, xRot);
    }

    public double getCameraYRot() {
        return Mth.lerp(yRotAnimation.getValue(), prevYRot, yRot);
    }

    public double getMaxRotationXDegrees() {
        return 40.0;
    }

    public double getMaxRotationYDegrees() {
        return 30.0;
    }

    public double getRotationXStepDegrees() {
        return 5.0;
    }

    public double getRotationYStepDegrees() {
        return 5.0;
    }

    public void toggle() {
        if (camera instanceof CameraOnStand) {
            Minecrft.options().setCameraType(CameraType.FIRST_PERSON);
            return;
        }

        CameraType newCameraType = Minecrft.options().getCameraType() == CameraType.FIRST_PERSON
                ? CameraType.THIRD_PERSON_FRONT
                : CameraType.FIRST_PERSON;

        Minecrft.options().setCameraType(newCameraType);
    }

    public void updateSelfieMode() {
        boolean inSelfieMode = Minecrft.options().getCameraType() == CameraType.THIRD_PERSON_FRONT;
        if (camera.inSelfieMode() != inSelfieMode) {
            CameraSettings.SELFIE_MODE.setAndSync(camera, inSelfieMode);
            CameraSettings.SELFIE_ROTATION_X.setAndSync(camera, 0.0);
            CameraSettings.SELFIE_ROTATION_Y.setAndSync(camera, 0.0);
            xRot = 0;
            yRot = 0;
            rawXRot = 0;
            rawYRot = 0;
        }
    }

    public boolean mouseMove(double xRotDelta, double yRotDelta) {
        if (!viewfinder.controlsActive() && camera.inSelfieMode() && Minecrft.options().keySprint.isDown) {
            rotateCamera(xRotDelta, yRotDelta, false);
            return true;
        }

        return false;
    }

    public void rotateCamera(double xRotDelta, double yRotDelta, boolean precise) {
        double maxX = getMaxRotationXDegrees();
        double maxY = getMaxRotationYDegrees();
        this.rawXRot = Mth.clamp(this.rawXRot + xRotDelta * 0.15, -maxX, maxX);
        this.rawYRot = Mth.clamp(this.rawYRot + yRotDelta * 0.15, -maxY, maxY);

        double stepX = getRotationXStepDegrees() * (precise ? 0.25 : 1);
        double stepY = getRotationYStepDegrees() * (precise ? 0.25 : 1);
        double xr = ((int)this.rawXRot) - ((int)this.rawXRot) % stepX;
        double yr = ((int)this.rawYRot) - ((int)this.rawYRot) % stepY;

        if (xr != prevXRot) {
            this.prevXRot = getCameraXRot();
            this.xRot = xr;
            xRotAnimation.resetProgress();
            CameraSettings.SELFIE_ROTATION_X.setAndSync(camera, this.xRot);
        }

        if (yr != prevYRot) {
            this.prevYRot = getCameraYRot();
            this.yRot = yr;
            yRotAnimation.resetProgress();
            CameraSettings.SELFIE_ROTATION_Y.setAndSync(camera, this.yRot);
        }
    }
}