package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.client.render.ExposureRenderer;

public class ExposureClient {
    private static final ExposureRenderer exposureRenderer = new ExposureRenderer();
    public static void init() {}

    public static ExposureRenderer getExposureRenderer() {
        return exposureRenderer;
    }
}
