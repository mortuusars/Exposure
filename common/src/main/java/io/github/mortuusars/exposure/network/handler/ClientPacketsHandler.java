package io.github.mortuusars.exposure.network.handler;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.gui.screen.FilmFrameInspectScreen;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.sound.UniqueSoundManager;
import io.github.mortuusars.exposure.client.sound.instance.ShutterTickingSoundInstance;
import io.github.mortuusars.exposure.client.task.ExportExposuresTask;
import io.github.mortuusars.exposure.client.task.ExposureRetrieveTask;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplate;
import io.github.mortuusars.exposure.client.image.TrichromeImage;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplates;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.capture.saving.ExposureUploader;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.network.packet.clientbound.*;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientPacketsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void applyShader(ShaderApplyS2CP packet) {
        packet.shaderLocation().ifPresentOrElse(
                shader -> Minecrft.get().gameRenderer.loadEffect(shader),
                () -> Minecrft.get().gameRenderer.shutdownEffect());
    }

    public static void showExposure(ShowExposureCommandS2CP packet) {
        if (packet.negative()) {
            Screen screen = new FilmFrameInspectScreen(packet.frames());
            Minecrft.get().setScreen(screen);
            return;
        }

        List<ItemAndStack<PhotographItem>> photographs = new ArrayList<>(packet.frames().stream().map(frame -> {
            ItemStack photographStack = new ItemStack(Exposure.Items.PHOTOGRAPH.get());
            photographStack.set(Exposure.DataComponents.PHOTOGRAPH_FRAME, frame);
            return new ItemAndStack<PhotographItem>(photographStack);
        }).toList());
        Collections.reverse(photographs);

        Screen screen = new PhotographScreen(photographs);
        Minecrft.get().setScreen(screen);
    }

    public static void exportExposures(ExportS2CP packet) {
        ExportExposuresTask.start(packet.ids(), packet.size(), packet.look());
    }

    public static void stopExportTask() {
        if (!ExportExposuresTask.stopCurrentTask()) {
            Minecrft.playerOrThrow().displayClientMessage(Component.translatable("task.exposure.export.not_running")
                    .withStyle(ChatFormatting.RED), false);
        }
    }

    public static void clearRenderingCache() {
        ExposureClient.imageRenderer().clearCache();
        ExposureClient.renderedExposures().clearCache();
    }

    public static void exposureDataChanged(ExposureDataChangedS2CP packet) {
        ExposureClient.exposureStore().refresh(packet.id());
        ExposureClient.imageRenderer().clearCacheOf(packet.id());
        ExposureClient.renderedExposures().clearCacheOf(packet.id());
    }

    public static void createChromaticExposure(CreateChromaticExposureS2CP packet) {
        if (packet.id().isEmpty()) {
            LOGGER.error("Cannot create chromatic exposure: identifier is empty.");
            return;
        }

        if (packet.layers().size() != 3) {
            LOGGER.error("Cannot create chromatic exposure: 3 layers required. Provided: '{}'.", packet.layers().size());
            return;
        }

        Holder<ColorPalette> colorPalette = ColorPalettes.getDefault(Minecrft.registryAccess());
        ColorPalette palette = colorPalette.value();
        ResourceLocation paletteId = colorPalette.unwrapKey().orElseThrow().location();

        ExposureClient.cycles().addParallelTask(new ExposureRetrieveTask(packet.layers(), 20_000)
                .then(Result::unwrap)
                .thenAsync(layers -> (Image) new TrichromeImage(layers.get(0), layers.get(1), layers.get(2)))
                .thenAsync(Palettizer.DITHERED.palettizeAndClose(palette))
                .thenAsync(img -> new ExposureData(img.width(), img.height(), img.pixels(), paletteId,
                        new ExposureData.Tag(ExposureType.COLOR, Minecrft.playerOrThrow().getScoreboardName(),
                                UnixTimestamp.Seconds.now(), false, false)))
                .acceptAsync(ExposureUploader.upload(packet.id())));
    }

    public static void startCapture(CaptureStartS2CP packet) {
        Task<?> captureTask = CaptureTemplates.getOrThrow(packet.templateId()).createTask(packet.captureParameters());
        ExposureClient.cycles().enqueueTask(captureTask);
    }

    public static void startDebugRGBCapture(CaptureStartDebugRGBS2CP packet) {
        CaptureTemplate template = CaptureTemplates.getOrThrow(packet.templateId());

        for (CaptureParameters captureParameters : packet.captureProperties()) {
            Task<?> captureTask = template.createTask(captureParameters);
            ExposureClient.cycles().enqueueTask(captureTask);
        }
    }

    public static void shutterOpened() {
        if (CameraClient.viewfinder() != null) {
            CameraClient.viewfinder().overlay().startDrawingShutter();
        }
    }

    public static void playShutterTickingSound(UniqueSoundPlayShutterTickingS2CP packet) {
        Entity entity = Minecrft.playerOrThrow().level().getEntity(packet.entityId());
        if (entity != null) {
            SoundInstance instance = new ShutterTickingSoundInstance(entity, packet.cameraId(),
                    Exposure.SoundEvents.SHUTTER_TICKING.get(), entity.getSoundSource(),
                    packet.volume(), packet.pitch(), packet.durationTicks());
            UniqueSoundManager.play(packet.cameraId().toString(), instance);
        }
    }

    public static void stopControllingCameraStand(CameraStandStopControllingS2CP packet) {
        if (Minecrft.get().cameraEntity != Minecrft.playerOrThrow() && Minecrft.levelOrThrow().getEntity(packet.standId()) instanceof CameraStandEntity stand) {
            stand.stopControlling();
            CameraClient.setCameraEntity(Minecrft.playerOrThrow());
        }
    }
}
