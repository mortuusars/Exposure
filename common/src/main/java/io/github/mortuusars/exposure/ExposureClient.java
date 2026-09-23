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
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.integration.ModCompatibilityClient;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.photograph.PhotographType;
import io.github.mortuusars.exposure.util.cycles.Cycles;
import io.github.mortuusars.exposure.client.ExposureStore;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.item.ChromaticSheetItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class ExposureClient {
    private static final Cycles CYCLES = new Cycles();
    private static final ExposureStore EXPOSURE_STORE = new ExposureStore();
    private static final RenderedExposures RENDERED_EXPOSURES = new RenderedExposures();
    private static final ImageRenderer IMAGE_RENDERER = new ImageRenderer();
    private static final PhotographRenderer PHOTOGRAPH_RENDERER = new PhotographRenderer();

    public static void init() {
        CameraModelPoses.register(Exposure.Items.CAMERA.get(), new CameraPoses());

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

        registerItemModelProperties();

        ModCompatibilityClient.init();
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
        //TODO: maybe check if neoforge is ok and then enable it only on fabric?
        if (PlatformHelper.isModLoaded("distanthorizons")
                && (PlatformHelper.isModLoaded("oculus") || PlatformHelper.isModLoaded("iris"))) {
            return true;
        }

        return Config.Client.FORCE_DIRECT_CAPTURE.isTrue()
                || Config.Client.FORCE_DIRECT_CAPTURE_MODS.get().stream().anyMatch(PlatformHelper::isModLoaded);
    }

    // --

    private static void registerItemModelProperties() {
        ItemProperties.register(Exposure.Items.CAMERA.get(), Exposure.resource("camera_gold"), (stack, level, entity, seed) ->
                stack.getOrDefault(Exposure.DataComponents.CAMERA_GOLD, false) ? 1 : 0);

        ItemProperties.register(Exposure.Items.CAMERA.get(), Exposure.resource("camera_active"), (stack, level, entity, seed) ->
                stack.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(stack) ? 1 : 0);

        ItemProperties.register(Exposure.Items.CAMERA.get(), Exposure.resource("camera_selfie"), (stack, level, entity, seed) ->
                stack.getItem() instanceof CameraItem cameraItem && cameraItem.isInSelfieMode(stack)
                        ? entity == Minecrft.get().getCameraEntity() ? 0.5f : 1f
                        : 0);

        ItemProperties.register(Exposure.Items.CAMERA.get(), Exposure.resource("camera_has_lens"), (stack, level, entity, seed) ->
                !Attachment.LENS.get(stack).isEmpty() ? 1 : 0);

        ItemProperties.register(Exposure.Items.CAMERA.get(), Exposure.resource("camera_has_flash"), (stack, level, entity, seed) ->
                !Attachment.FLASH.get(stack).isEmpty() ? 1 : 0);

        ItemProperties.register(Exposure.Items.CHROMATIC_SHEET.get(), Exposure.resource("channels"), (stack, clientLevel, livingEntity, seed) ->
                stack.getItem() instanceof ChromaticSheetItem chromaticSheet ?
                        chromaticSheet.getLayers(stack).size() / 10f : 0f);

        ItemProperties.register(Exposure.Items.STACKED_PHOTOGRAPHS.get(), Exposure.resource("count"),
                (stack, clientLevel, livingEntity, seed) ->
                        stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem ?
                                stackedPhotographsItem.getPhotographs(stack).size() / 100f : 0f);

        ItemProperties.register(Exposure.Items.ALBUM.get(), Exposure.resource("photos"),
                (stack, clientLevel, livingEntity, seed) ->
                        stack.getItem() instanceof AlbumItem albumItem ? albumItem.getPhotographsCount(stack) / 100f : 0f);

        ItemProperties.register(Exposure.Items.INTERPLANAR_PROJECTOR.get(), Exposure.resource("projector_active"),
                (stack, clientLevel, livingEntity, seed) -> Config.Server.CAN_PROJECT.get() && stack.has(DataComponents.CUSTOM_NAME) ? 1f : 0f);
    }

    public static class Models {
        public static final Set<ModelResourceLocation> MODELS = new HashSet<>();

        public static final ModelResourceLocation CAMERA_GUI = register("item/camera_gui");
        public static final ModelResourceLocation CAMERA_ACTIVE = register("item/camera_active");
        public static final ModelResourceLocation CAMERA_SELFIE = register("item/camera_selfie");
        public static final ModelResourceLocation CAMERA_VIEWFINDER = register("item/camera_parts/viewfinder");
        public static final ModelResourceLocation CAMERA_FLASH = register("item/camera_parts/flash");
        public static final ModelResourceLocation CAMERA_LENS = register("item/camera_parts/lens");
        public static final ModelResourceLocation CAMERA_SELFIE_STICK = register("item/camera_parts/selfie_stick");
        public static final ModelResourceLocation SELFIE_STICK = register("item/selfie_stick");

        public static final ModelResourceLocation PHOTOGRAPH_FRAME_SMALL = register("block/photograph_frame_small");
        public static final ModelResourceLocation PHOTOGRAPH_FRAME_MEDIUM = register("block/photograph_frame_medium");
        public static final ModelResourceLocation PHOTOGRAPH_FRAME_LARGE = register("block/photograph_frame_large");
        public static final ModelResourceLocation CLEAR_PHOTOGRAPH_FRAME_SMALL = register("block/glass_photograph_frame_small");
        public static final ModelResourceLocation CLEAR_PHOTOGRAPH_FRAME_MEDIUM = register("block/glass_photograph_frame_medium");
        public static final ModelResourceLocation CLEAR_PHOTOGRAPH_FRAME_LARGE = register("block/glass_photograph_frame_large");
        public static final ModelResourceLocation CAMERA_STAND = register("block/camera_stand");
        public static final ModelResourceLocation CAMERA_STAND_MOUNT = register("block/camera_stand_mount");

        public static ModelResourceLocation register(String path) {
            ModelResourceLocation location = new ModelResourceLocation(Exposure.resource(path), "standalone");
            MODELS.add(location);
            return location;
        }
    }

    public static class Textures {
        public static final ResourceLocation EMPTY = Exposure.resource("textures/empty.png");

        public static class Photograph {
            public static final ResourceLocation REGULAR_PAPER = Exposure.resource("textures/photograph/photograph.png");
            public static final ResourceLocation REGULAR_ALBUM_PAPER = Exposure.resource("textures/photograph/photograph_album.png");

            public static final ResourceLocation AGED_PAPER = Exposure.resource("textures/photograph/aged_photograph.png");
            public static final ResourceLocation AGED_OVERLAY = Exposure.resource("textures/photograph/aged_photograph_overlay.png");
            public static final ResourceLocation AGED_ALBUM_PAPER = Exposure.resource("textures/photograph/aged_photograph_album.png");
            public static final ResourceLocation AGED_ALBUM_OVERLAY = Exposure.resource("textures/photograph/aged_photograph_album_overlay.png");
        }
    }
}
