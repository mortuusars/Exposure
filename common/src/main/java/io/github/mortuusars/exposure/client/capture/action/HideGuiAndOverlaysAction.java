package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.mortaar.client.Minecrft;

public class HideGuiAndOverlaysAction implements CaptureAction {
    public static boolean shouldBlockDebugRenderer;

    protected boolean hideGuiBeforeCapture;

    @Override
    public void beforeCapture() {
        hideGuiBeforeCapture = Minecrft.options().hideGui;

        Minecrft.options().hideGui = true;
        shouldBlockDebugRenderer = true;
    }

    @Override
    public void afterCapture() {
        Minecrft.options().hideGui = hideGuiBeforeCapture;
        shouldBlockDebugRenderer = false;
    }
}
