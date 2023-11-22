package io.github.mortuusars.exposure.camera.capture.component;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Client-side extension of the capture system. Allows modifying the steps of the capture process.
 */
@SuppressWarnings("unused")
public interface ICaptureComponent {

    /**
     * Allows to delay the capture by a number of game ticks.
     * Largest delay from all modifiers will be used.
     * Called before {@link #initialize initialize}.
     */
    default int getTicksDelay(Capture capture) {
        return 0;
    }

    /**
     * Allows to delay the capture by a number of render frames. Frame is a tick of a 'TickEvent.RenderTickEvent'.<br>
     * Largest delay from all modifiers will be used.<br>
     * Called before {@link #initialize initialize} and after {@link #getTicksDelay getTicksDelay}
     */
    default int getFramesDelay(Capture capture) {
        return 0;
    }

    /**
     * Called when the Capture is initialized. Delays are determined at this point.
     */
    default void initialize(Capture capture) { }

    /**
     * Called for every game tick while it's on a delay (before taking a shot). This method is guaranteed to be called at least once.
     */
    default void onDelayTick(Capture capture, int delayTicksLeft) { }

    /**
     * Called on every RENDER tick while it's on a delay (before taking a shot). This method is guaranteed to be called at least once.
     */
    default void onDelayFrame(Capture capture, int delayFramesLeft) { }

    /**
     * Called after screenshot has been taken and before any processing of the image.
     */
    default void screenshotTaken(Capture capture, NativeImage screenshot) { }

    /**
     * Allows modifying a single pixel. Will be called for every pixel in the image.
     * @param red 0 - 255
     * @param green 0 - 255
     * @param blue 0 - 255
     * @return Vector of RGB colors (0 - 255).
     */
    default Color modifyPixel(Capture capture, int red, int green, int blue) {
        return new Color(red, green, blue);
    }

    /**
     * Allows modifying an image after it has been processed. Before dithering.
     */
    default BufferedImage modifyImage(Capture capture, BufferedImage image) {
        return image;
    }

    /**
     * Called almost at the end, after an image has been processed but before saving.
     */
    default void teardown(Capture capture) { }

    default void save(byte[] MapColorPixels, int width, int height, FilmType filmType) { }

    /**
     * Called in the end, regardless of the success or not.
     */
    default void end(Capture capture) { }
}
