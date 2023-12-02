package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.client.renderer.NewExposureRenderer;
import io.github.mortuusars.exposure.client.renderer.ExposureOnPaperRenderer;

public class ExposureClient {
    private static final NewExposureRenderer exposureRenderer = new NewExposureRenderer();
    private static final ExposureOnPaperRenderer exposureOnPaperRenderer = new ExposureOnPaperRenderer(exposureRenderer);
    public static void init() {}

    public static NewExposureRenderer getExposureRenderer() {
        return exposureRenderer;
    }

    public static ExposureOnPaperRenderer getExposureOnPaperRenderer() {
        return exposureOnPaperRenderer;
    }
}
