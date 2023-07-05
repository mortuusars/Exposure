package io.github.mortuusars.exposure.camera.modifier;

import io.github.mortuusars.exposure.camera.Capture;

import java.awt.*;
import java.awt.image.BufferedImage;

@SuppressWarnings("unused")
public interface IExposureModifier {
    String id();

    /**
     * Allows modifying the time before exposure. Used for flash to wait for light update before taking a shot, for example.
     * Largest delay from all modifiers will be used. Called in the setup phase.
     * @return delay in render ticks (TickEvent.RenderTickEvent)
     */
    default int getCaptureDelay(Capture properties) {
        return 0;
    }

    /**
     * Called when the capture is requested. Before everything.
     */
    default void setup(Capture properties) { }

    /**
     * Called on every delay tick (before taking a shot). If something needs to be done continuously before a shot.
     */
    default void onSetupDelayTick(Capture properties, int ticksLeft) {}

    /**
     * Modifies a single pixel. Will be called for every pixel in the image.
     * @param red 0 - 255
     * @param green 0 - 255
     * @param blue 0 - 255
     * @return Vector of RGB colors (0 - 255).
     */
    default Color modifyPixel(Capture properties, int red, int green, int blue) {
        return new Color(red, green, blue);
    }

    /**
     * Modifies an image after it has been processed. Before dithering.
     */
    default BufferedImage modifyImage(Capture properties, BufferedImage image) {
        return image;
    }

    /**
     * Called almost at the end, after an image has been processed but before saving.
     */
    default void teardown(Capture properties) { }

    /**
     * Called in the end, after an image has been processed and saved.
     */
    default void end(Capture properties) { }
}
