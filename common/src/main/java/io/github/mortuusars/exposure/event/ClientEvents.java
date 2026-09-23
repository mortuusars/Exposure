package io.github.mortuusars.exposure.event;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.ItemFramePhotographRenderer;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.decoration.ItemFrame;

public class ClientEvents {
    public static void levelUnloaded() {
    }

    public static void login() {
        /*try {
            preloadStuffToFixLagSpikes();
        } catch (Exception e) {
            Exposure.LOGGER.warn("Failed to preload stuff: {}", e.getMessage());
        }*/
    }

    /* Not sure that this is needed anymore. Doesn't lag as much now.
    private static void preloadStuffToFixLagSpikes() {
        ClientPacketsHandler.clearRenderingCache();
        boolean active = Minecrft.player().getActiveExposureCameraOptional().isEmpty();

        EasingFunction.EASE_OUT_EXPO.ease(0.5);
        ViewfinderRegistry.getConstructor(Exposure.Items.CAMERA.get()).apply(new Camera(Minecrft.player(), CameraId.create()) {
            @Override
            public ItemStack getItemStack() { return new ItemStack(Exposure.Items.CAMERA.get()); }
            @Override
            public Packet createSyncPacket() { return null; }
        });
        CameraClient.removeViewfinder();

        CaptureTemplates.get(Exposure.resource("dummy"));
        CameraCaptureTemplate cameraCaptureTemplate = new CameraCaptureTemplate();
        ExposureClient.cycles().enqueueTask(new PreloadingDummyCaptureTemplate()
                .createTask(new CaptureParameters.Builder("dummy").build()));
        UniqueSoundManager.stop(Minecrft.player().getScoreboardName(), Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get());
    }*/

    public static void disconnect() {
        resetRenderData();
    }

    public static void resetRenderData() {
        ExposureClient.exposureStore().clear();
        ExposureClient.renderedExposures().clearCache();
        ExposureClient.imageRenderer().clearCache();
    }

    public static boolean renderItemFrameItem(ItemFrame itemFrame, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!Config.Client.PHOTOGRAPH_RENDERS_IN_ITEM_FRAME.get()) return false;
        if (!(itemFrame.getItem().getItem() instanceof PhotographItem photographItem)) return false;
        if (photographItem.getFrame(itemFrame.getItem()).identifier().isEmpty()) return false;

        poseStack.pushPose();
        poseStack.scale(2F, 2F, 2F);
        ItemFramePhotographRenderer.render(itemFrame, poseStack, buffer, packedLight, photographItem, itemFrame.getItem());
        poseStack.popPose();

        return true;
    }

    public static void resourcesReloaded() {
        ExposureClient.exposureStore().clear();
        ExposureClient.renderedExposures().clearCache();
        ExposureClient.imageRenderer().clearCache();
    }
}
