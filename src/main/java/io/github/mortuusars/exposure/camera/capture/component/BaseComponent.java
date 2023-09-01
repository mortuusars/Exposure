package io.github.mortuusars.exposure.camera.capture.component;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.camera.capture.Capture;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class BaseComponent implements ICaptureComponent {
    private final boolean hideGuiValue;
    private final CameraType cameraTypeValue;
    private boolean guiHidden;
    private CameraType cameraType;

    public BaseComponent(boolean hideGuiValue, CameraType cameraTypeValue) {
        this.hideGuiValue = hideGuiValue;
        this.cameraTypeValue = cameraTypeValue;
        guiHidden = false;
        cameraType = CameraType.FIRST_PERSON;
    }

    public BaseComponent() {
        this(true, CameraType.FIRST_PERSON);
    }

    @Override
    public void onDelayFrame(Capture capture, int delayFramesLeft) {
        if (delayFramesLeft == 0) { // Right before capturing
            Minecraft mc = Minecraft.getInstance();
            guiHidden = mc.options.hideGui;
            cameraType = mc.options.getCameraType();

            mc.options.hideGui = hideGuiValue;
            mc.options.setCameraType(cameraTypeValue);
        }
    }

    @Override
    public void screenshotTaken(Capture capture, NativeImage screenshot) {
        Minecraft mc = Minecraft.getInstance();
        mc.options.hideGui = guiHidden;
        mc.options.setCameraType(cameraType);
    }
}
