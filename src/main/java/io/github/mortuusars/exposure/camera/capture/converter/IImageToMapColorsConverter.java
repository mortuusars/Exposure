package io.github.mortuusars.exposure.camera.capture.converter;

import io.github.mortuusars.exposure.camera.capture.Capture;

import java.awt.image.BufferedImage;

public interface IImageToMapColorsConverter {
    byte[] convert(Capture capture, BufferedImage image);
}
