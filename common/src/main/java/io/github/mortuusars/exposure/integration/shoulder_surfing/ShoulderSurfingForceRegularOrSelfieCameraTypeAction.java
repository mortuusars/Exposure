package io.github.mortuusars.exposure.integration.shoulder_surfing;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.Perspective;
import io.github.mortuusars.exposure.client.capture.action.ForceRegularOrSelfieCameraTypeAction;
import io.github.mortuusars.exposure.world.entity.CameraHolder;

public class ShoulderSurfingForceRegularOrSelfieCameraTypeAction extends ForceRegularOrSelfieCameraTypeAction {
    private boolean isShoulderSurfingBeforeCapture = false;

    public ShoulderSurfingForceRegularOrSelfieCameraTypeAction(CameraHolder holder) {
        super(holder);
    }

    @Override
    public void beforeCapture() {
        isShoulderSurfingBeforeCapture = IShoulderSurfing.getInstance().isShoulderSurfing();
        super.beforeCapture();
    }

    @Override
    public void afterCapture() {
        Perspective perspectiveBefore = Perspective.of(cameraTypeBeforeCapture, isShoulderSurfingBeforeCapture);
        IShoulderSurfing.getInstance().changePerspective(perspectiveBefore);
    }
}
