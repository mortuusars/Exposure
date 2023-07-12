package io.github.mortuusars.exposure.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Photograph;
import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import io.github.mortuusars.exposure.client.screen.CameraScreen;
import io.github.mortuusars.exposure.client.screen.DarkroomScreen;
import io.github.mortuusars.exposure.client.viewfinder.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.client.viewfinder.ViewfinderRenderer;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.UpdateActiveCameraPacket;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Optional;

public class ClientEvents {
    public static class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(Exposure.MenuTypes.CAMERA.get(), CameraScreen::new);
                MenuScreens.register(Exposure.MenuTypes.DARKROOM.get(), DarkroomScreen::new);
            });
        }
    }

    public static class ForgeBus {
        @SubscribeEvent
        public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ExposureStorage.CLIENT.clear();
        }

        @SubscribeEvent
        public static void renderOverlay(RenderGuiOverlayEvent.Pre event) {
            if (isLookingThroughViewfinder()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void renderHand(RenderHandEvent event) {
            if (isLookingThroughViewfinder() || !ViewfinderRenderer.fovRestored) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void renderPlayer(RenderPlayerEvent.Pre event) {
            Player player = event.getEntity();
            if (Exposure.getCamera().isActive(player))
                player.startUsingItem(player.getMainHandItem()
                        .getItem() instanceof CameraItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        }

        @SubscribeEvent
        public static void onGuiOpen(ScreenEvent.Opening event) {
            if (isLookingThroughViewfinder() && !(event.getNewScreen() instanceof ViewfinderControlsScreen)) {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    Exposure.getCamera().deactivate(player);
                    Packets.sendToServer(new UpdateActiveCameraPacket(player.getUUID(), false, player.getMainHandItem()
                            .getItem() instanceof CameraItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                }
            }
        }

        private static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=true");

        @SubscribeEvent
        public static void renderItemFrameItem(RenderItemInFrameEvent event) {
            ItemStack itemStack = event.getItemStack();
            if (!(itemStack.getItem() instanceof PhotographItem photographItem))
                return;

            Photograph photographData = photographItem.getPhotographData(itemStack);
            Optional<ExposureSavedData> queriedExposureData = Exposure.getStorage().getOrQuery(photographData.getId());
            if (queriedExposureData.isEmpty())
                return;

            //TODO: GLOW

            ExposureSavedData exposureSavedData = queriedExposureData.get();
            PoseStack poseStack = event.getPoseStack();

            // TODO: BORDER/NO BORDER

            ModelManager modelmanager = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager();
            ModelResourceLocation modelresourcelocation = new ModelResourceLocation("item_frame", "map=true");
            poseStack.pushPose();
            poseStack.translate(-0.5D, -0.5D, -0.46875D * 2f);
            int packedLight = event.getItemFrameEntity().getType() == EntityType.GLOW_ITEM_FRAME ? LightTexture.FULL_BRIGHT : event.getPackedLight();
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), event.getMultiBufferSource().getBuffer(Sheets.solidBlockSheet()), (BlockState)null, modelmanager.getModel(modelresourcelocation), 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();

//            float scale = 0.0078125F / 2f;
//            float pixelSize = exposureSavedData.getWidth() / 16f;
            float scale = 1f / exposureSavedData.getWidth();
            float pixelScale = scale / 16f;
            scale -= pixelScale * 2;

            poseStack.pushPose();
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-exposureSavedData.getWidth() / 2f, -exposureSavedData.getHeight() / 2f, -1);
//            poseStack.translate(0.0D, 0.0D, -1.0D);
            ExposureRenderer.render(poseStack, event.getMultiBufferSource(), photographData.getId(), exposureSavedData, packedLight);
            poseStack.popPose();
            event.setCanceled(true);
        }

        private static boolean isLookingThroughViewfinder() {
            return Exposure.getCamera().isActive(Minecraft.getInstance().player);
        }
    }
}
