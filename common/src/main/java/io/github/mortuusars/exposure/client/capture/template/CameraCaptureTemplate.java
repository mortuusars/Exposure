package io.github.mortuusars.exposure.client.capture.template;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.capture.saving.ExposureUploader;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.capture.Projection;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.cycles.task.EmptyTask;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class CameraCaptureTemplate implements CaptureTemplate {
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

        Task<ExposureData> captureTask = Capture.of(Capture.screenshot(),
                        CaptureAction.setCameraEntity(entity),
                        CaptureAction.forceRegularOrSelfieCamera(holder),
                        CaptureAction.optional(params.fov(), CaptureAction::setFov),
                        CaptureAction.hideGui(),
                        CaptureAction.optional(Config.Client.KEEP_POST_EFFECT.isFalse(), CaptureAction::disablePostEffect),
                        CaptureAction.setFilter(params.filter()),
                        CaptureAction.modifyGamma(params.getShutterSpeed()),
                        CaptureAction.optional(params.getFlash(), () -> CaptureAction.flash(entity)))
                .handleErrorAndGetResult(printCasualErrorInChat())
                .thenAsync(applyEffectsToImage(params))
                .thenAsync(Palettizer.fromDitherMode(params.filmProperties().ditherMode()).palettizeAndClose(palette.value()))
                .then(convertToExposureData(palette, createExposureTag(params, false)));

        if (params.projection().isPresent()) {
            Projection projection = params.projection().get();
            String path = projection.path();

            captureTask = captureTask.overridenBy(Capture.of(Capture.path(path),
                            CaptureAction.optional(params.cameraId(), CaptureAction::interplanarProjection))
                    .logErrorAndGetResult(LOGGER)
                    .thenAsync(applyEffectsToImage(params.mutable().setCropFactor(1f).build())) // Remove crop factor for loaded images
                    .thenAsync(Palettizer.fromDitherMode(projection.mode()).palettizeAndClose(palette.value()))
                    .then(convertToExposureData(palette, createExposureTag(params, true))));
        }

        return captureTask
                .acceptAsync(image -> ExposureUploader.upload(params.exposureId(), image))
                .onError(printCasualErrorInChat());
    }
}
