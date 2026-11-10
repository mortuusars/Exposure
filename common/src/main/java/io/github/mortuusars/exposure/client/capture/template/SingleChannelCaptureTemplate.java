package io.github.mortuusars.exposure.client.capture.template;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.capture.saving.ExposureUploader;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.cycles.task.EmptyTask;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SingleChannelCaptureTemplate implements CaptureTemplate {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public Task<?> createTask(CaptureParameters params) {
        if (params.exposureId().isEmpty()) {
            LOGGER.error("Failed to create capture task: exposure id cannot be empty. '{}'", params);
            return new EmptyTask<>();
        }

        @Nullable Entity entity = Minecrft.levelOrThrow().getEntity(params.cameraHolderId().orElse(Minecrft.playerOrThrow().getId()));
        if (entity == null) {
            LOGGER.error("Failed to create capture task: camera holder entity cannot be obtained. '{}'", params);
            return new EmptyTask<>();
        }

        @Nullable CameraHolder holder = entity instanceof CameraHolder cameraHolder ? cameraHolder : null;

        Holder<ColorPalette> palette = getColorPalette(params);

        return Capture.of(Capture.screenshot(),
                        CaptureAction.setCameraEntity(entity),
                        CaptureAction.forceRegularOrSelfieCamera(holder),
                        CaptureAction.optional(params.fov(), CaptureAction::setFov),
                        CaptureAction.hideGui(),
                        CaptureAction.optional(params.singleChannel(), channel -> CaptureAction.setPostEffect(channel.getShader())),
                        CaptureAction.setFilter(params.filter()),
                        CaptureAction.modifyGamma(params.getShutterSpeed()),
                        CaptureAction.optional(params.getFlash(), () -> CaptureAction.flash(entity)))
                .handleErrorAndGetResult(printCasualErrorInChat())
                .then(applyEffectsToImage(params))
                .thenAsync(Palettizer.fromDitherMode(params.filmProperties().ditherMode()).palettizeAndClose(palette.value()))
                .then(convertToExposureData(palette, createExposureTag(params, false)))
                .accept(image -> ExposureUploader.upload(params.exposureId(), image))
                .onError(printCasualErrorInChat());
    }
}
