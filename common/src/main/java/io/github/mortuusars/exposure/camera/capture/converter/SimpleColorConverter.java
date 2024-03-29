package io.github.mortuusars.exposure.camera.capture.converter;

import io.github.mortuusars.exposure.camera.capture.Capture;
import io.github.mortuusars.exposure.camera.capture.processing.RGBToMapColorConverter;

import java.awt.image.BufferedImage;

public class SimpleColorConverter implements IImageToMapColorsConverter {
    @Override
    public byte[] convert(Capture capture, BufferedImage image) {
        return RGBToMapColorConverter.convert(image);
    }
}
