package io.github.mortuusars.exposure.camera.modifier;

import io.github.mortuusars.exposure.camera.CameraCapture;
import io.github.mortuusars.exposure.camera.Capture;
import io.github.mortuusars.exposure.camera.component.ShutterSpeed;
import net.minecraft.util.Mth;

import java.awt.*;

public record BrightnessModifier(String id) implements IExposureModifier {
    @Override
    public int getCaptureDelay(Capture properties) {
        ShutterSpeed defaultShutterSpeed = properties.camera.getItem()
                .getDefaultShutterSpeed(properties.camera.getStack());

        // Changing the gamma is not applied instantly for some reason. Delay of 1 seem to fix it.
        return properties.shutterSpeed.getStopsDifference(defaultShutterSpeed) > 0 ? 1 : 0;
    }

    @Override
    public void setup(Capture properties) {
        ShutterSpeed shutterSpeed = properties.shutterSpeed;
        float stopsDifference = shutterSpeed.getStopsDifference(properties.camera.getItem().getDefaultShutterSpeed(properties.camera.getStack()));
        if (stopsDifference >= 0.89f) {
            CameraCapture.additionalBrightness = 0.0075f * stopsDifference;
        }
    }

    @Override
    public Color modifyPixel(Capture properties, int red, int green, int blue) {
        ShutterSpeed shutterSpeed = properties.shutterSpeed;
        float stopsDif = shutterSpeed.getStopsDifference(properties.camera.getItem().getDefaultShutterSpeed(properties.camera.getStack())) * 0.4f;

        // Shorter Shutter Speeds have less impact on the brightness:
        if (stopsDif < 0f)
            stopsDif *= 0.5f;

        float brightness = 1f + stopsDif;

        float r = red * brightness;
        float g = green * brightness;
        float b = blue * brightness;

        int[] rdst = redistribute(r, g, b);

        r = Mth.clamp(Math.round(r), 0, 255);
        g = Mth.clamp(Math.round(g), 0, 255);
        b = Mth.clamp(Math.round(b), 0, 255);

        // Two regular and redistributed values that are averaged together seem to give better result:
        return new Color(Mth.clamp((int)((r + rdst[0]) / 2), 0, 255),
                Mth.clamp((int)((g + rdst[1]) / 2), 0, 255),
                Mth.clamp((int)((b + rdst[2]) / 2), 0, 255));
    }

    @Override
    public void teardown(Capture properties) {
        CameraCapture.additionalBrightness = 0f;
    }


    /**
     * Redistributes excess (> 255) values to other channels.
     * Adapted from Mark Ransom's answer:
     * <a href="https://stackoverflow.com/a/141943">StackOverflow</a>
     */
    private int[] redistribute(float red, float green, float blue) {
        float threshold = 255.999f;
        float max = Math.max(red, Math.max(green, blue));
        if (max <= threshold) {
            return new int[] {
                    Mth.clamp(Math.round(red), 0, 255),
                    Mth.clamp(Math.round(green), 0, 255),
                    Mth.clamp(Math.round(blue), 0, 255) };
        }

        float total = red + green + blue;

        if (total >= 3 * threshold)
            return new int[] {(int) threshold, (int) threshold, (int) threshold};

        float x = (3f * threshold - total) / (3f * max - total);
        float gray = threshold - x * max;
        return new int[] {
                Mth.clamp(Math.round(gray + x * red), 0, 255),
                Mth.clamp(Math.round(gray + x * green), 0, 255),
                Mth.clamp(Math.round(gray + x * blue), 0, 255) };
    }
}
