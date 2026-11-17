package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.client.render.image.ImageRenderer;
import io.github.mortuusars.exposure.client.render.photograph.PhotographRenderer;

public interface ExposureGameRenderer {
    default ExposureStore getExposureStore() {
        throw new IllegalStateException("This method must be overriden.");
    }

    default RenderedExposures getRenderedExposures() {
        throw new IllegalStateException("This method must be overriden.");
    }

    default ImageRenderer getExposureImageRenderer() {
        throw new IllegalStateException("This method must be overriden.");
    }

    default PhotographRenderer getExposurePhotographRenderer() {
        throw new IllegalStateException("This method must be overriden.");
    }
}
