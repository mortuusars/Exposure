package io.github.mortuusars.exposure.camera.capture.converter;

import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.camera.capture.processing.FloydDither;

import java.awt.image.BufferedImage;

public class DitheringColorConverter implements IImageToMapColorsConverter {
    @Override
    public byte[] convert(Capture capture, BufferedImage image) {
        return FloydDither.ditherWithMapColors(image);
    }
}
