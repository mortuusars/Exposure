package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class ForceRegularOrSelfieCameraTypeAction implements CaptureAction {
    private final CameraHolder holder;
    private CameraType cameraTypeBeforeCapture = CameraType.FIRST_PERSON;

    public ForceRegularOrSelfieCameraTypeAction(CameraHolder holder) {
        this.holder = holder;
    }

    @Override
    public void beforeCapture() {
        cameraTypeBeforeCapture = Minecraft.getInstance().options.getCameraType();
        if (cameraTypeBeforeCapture == CameraType.THIRD_PERSON_BACK) {
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        } else if (cameraTypeBeforeCapture == CameraType.THIRD_PERSON_FRONT && !cameraInHandInSelfieMode()) {
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    protected boolean cameraInHandInSelfieMode() {
        @Nullable CameraInHand camera = CameraInHand.find(holder);
        return camera != null && camera.map(CameraItem::isInSelfieMode).orElse(false);
    }

    @Override
    public void afterCapture() {
        Minecraft.getInstance().options.setCameraType(cameraTypeBeforeCapture);
    }
}
