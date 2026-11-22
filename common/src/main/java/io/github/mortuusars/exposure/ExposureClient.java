package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.client.animation.CameraModelPoses;
import io.github.mortuusars.exposure.client.animation.CameraPoses;
import io.github.mortuusars.exposure.client.capture.template.*;
import io.github.mortuusars.exposure.client.task.ClearStaleRenderedImagesIndefiniteTask;
import io.github.mortuusars.exposure.client.RenderedExposures;
import io.github.mortuusars.exposure.client.camera.viewfinder.*;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.render.image.ImageRenderer;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.render.photograph.PhotographRenderer;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyles;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.photograph.PhotographType;
import io.github.mortuusars.exposure.util.cycles.Cycles;
import io.github.mortuusars.exposure.client.ExposureStore;
import net.minecraft.resources.Identifier;

public class ExposureClient {
    private static final Cycles CYCLES = new Cycles();
    private static final ExposureStore EXPOSURE_STORE = new ExposureStore();
    private static final RenderedExposures RENDERED_EXPOSURES = new RenderedExposures();
    private static final ImageRenderer IMAGE_RENDERER = new ImageRenderer();
    private static final PhotographRenderer PHOTOGRAPH_RENDERER = new PhotographRenderer();

    public static void init() {
        CameraModelPoses.register(Exposure.Items.CAMERA.get(), new CameraPoses<>());

        ViewfinderRegistry.register(Exposure.Items.CAMERA.get(), Viewfinder::new);

        CaptureTemplates.register(CaptureType.CAMERA, new CameraCaptureTemplate());
        CaptureTemplates.register(CaptureType.EXPOSE_COMMAND, new ExposeCaptureTemplate());
        CaptureTemplates.register(CaptureType.LOAD_COMMAND, new PathCaptureTemplate());
        CaptureTemplates.register(CaptureType.DEBUG_RGB, new SingleChannelCaptureTemplate());

        PhotographStyles.register(PhotographType.REGULAR, PhotographStyle.REGULAR);
        PhotographStyles.register(PhotographType.AGED, new PhotographStyle(
                ExposureClient.Textures.Photograph.AGED_PAPER,
                ExposureClient.Textures.Photograph.AGED_OVERLAY,
                ExposureClient.Textures.Photograph.AGED_ALBUM_PAPER,
                ExposureClient.Textures.Photograph.AGED_ALBUM_OVERLAY,
                ImageEffect.AGED));

        cycles().addParallelTask(new ClearStaleRenderedImagesIndefiniteTask());
    }

    public static Cycles cycles() {
        return CYCLES;
    }

    public static ExposureStore exposureStore() {
        return EXPOSURE_STORE;
    }

    public static RenderedExposures renderedExposures() {
        return RENDERED_EXPOSURES;
    }

    public static ImageRenderer imageRenderer() {
        return IMAGE_RENDERER;
    }

    public static PhotographRenderer photographRenderer() {
        return PHOTOGRAPH_RENDERER;
    }

    // --

    public static boolean shouldUseDirectCapture() {
        //noinspection ConstantValue
        return Config.Client.FORCE_DIRECT_CAPTURE.isTrue() || Config.Client.FORCE_DIRECT_CAPTURE_MODS.get().stream().anyMatch(PlatformHelper::isModLoaded);
    }

    // --

    public static class Models {
        public static final Identifier PHOTOGRAPH_FRAME_SMALL =
                Exposure.resource("photograph_frame_small");
        public static final Identifier PHOTOGRAPH_FRAME_MEDIUM =
                Exposure.resource("photograph_frame_medium");
        public static final Identifier PHOTOGRAPH_FRAME_LARGE =
               Exposure.resource("photograph_frame_large");
        public static final Identifier CLEAR_PHOTOGRAPH_FRAME_SMALL =
                Exposure.resource("glass_photograph_frame_small");
        public static final Identifier CLEAR_PHOTOGRAPH_FRAME_MEDIUM =
                Exposure.resource("glass_photograph_frame_medium");
        public static final Identifier CLEAR_PHOTOGRAPH_FRAME_LARGE =
                Exposure.resource("glass_photograph_frame_large");
        public static final Identifier CAMERA_STAND =
                Exposure.resource("camera_stand");
        public static final Identifier CAMERA_STAND_MOUNT =
                Exposure.resource("camera_stand_mount");
    }

    public static class SelectProperties {
        public static final Identifier CAMERA_STATUS = Exposure.resource("camera_status");
        public static final Identifier CAMERA_ATTACHMENTS = Exposure.resource("camera_attachments");
    }

   public static class RangeSelectProperties {
        public static final Identifier ALBUM_PHOTOS = Exposure.resource("photos");
        public static final Identifier CHANNELS = Exposure.resource("channels");
       public static final Identifier COUNT = Exposure.resource("count");
   }

   public static class ConditionalProperties {
        public static final Identifier PROJECTOR_ACTIVE = Exposure.resource("projector_active");
   }

    public static class Textures {
        public static final Identifier EMPTY = Exposure.resource("textures/empty.png");

        public static class Photograph {
            public static final Identifier REGULAR_PAPER = Exposure.resource("textures/photograph/photograph.png");
            public static final Identifier REGULAR_ALBUM_PAPER = Exposure.resource("textures/photograph/photograph_album.png");

            public static final Identifier AGED_PAPER = Exposure.resource("textures/photograph/aged_photograph.png");
            public static final Identifier AGED_OVERLAY = Exposure.resource("textures/photograph/aged_photograph_overlay.png");
            public static final Identifier AGED_ALBUM_PAPER = Exposure.resource("textures/photograph/aged_photograph_album.png");
            public static final Identifier AGED_ALBUM_OVERLAY = Exposure.resource("textures/photograph/aged_photograph_album_overlay.png");
        }
    }
}
