package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.data.ColorPalettes;
import net.minecraft.client.CameraType;

import java.util.Optional;

public class PreloadingDummyCaptureTemplate implements CaptureTemplate {
    @Override
    public Task<?> createTask(CaptureParameters params) {
        ColorPalette palette = ColorPalettes.get(Minecrft.registryAccess(), ColorPalettes.DEFAULT).value();

        return Capture.of(Capture.screenshot(),
                        CaptureAction.hideGui(),
                        CaptureAction.forceCamera(CameraType.FIRST_PERSON),
                        CaptureAction.setFilter(Optional.empty()),
                        CaptureAction.setFov(50),
                        CaptureAction.forceRegularOrSelfieCamera(null),
                        CaptureAction.disablePostEffect(),
                        CaptureAction.modifyGamma(params.getShutterSpeed()))
                .handleErrorAndGetResult()
                .thenAsync(ImageEffect.chain(
                        ImageEffect.Crop.SQUARE_CENTER,
                        ImageEffect.Crop.factor(1),
                        ImageEffect.Resize.to(16),
                        ImageEffect.exposure(2),
                        ImageEffect.BLACK_AND_WHITE))
                .thenAsync(Palettizer.DITHERED.palettizeAndClose(palette))
                .thenAsync(img -> ExposureData.EMPTY);
    }
}
