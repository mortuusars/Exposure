package io.github.mortuusars.exposure.camera.modifier;

import io.github.mortuusars.exposure.camera.CaptureProperties;
import io.github.mortuusars.exposure.camera.IExposureModifier;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

public record BlackAndWhiteModifier(String id) implements IExposureModifier {
    @Override
    public Vec3i modifyPixel(CaptureProperties properties, int red, int green, int blue) {
        // Weights adding up to more than 1 - to make the image slightly brighter
        int luma = Mth.clamp((int) (0.4 * red + 0.6 * green + 0.15 * blue), 0, 255);

        // Slightly increase the contrast
        int contrast = 136;
        luma = Mth.clamp((luma - 128) * contrast / 128 + 128, 0, 255);

        return new Vec3i(luma, luma, luma);
    }
}
