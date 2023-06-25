package io.github.mortuusars.exposure.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderOld;
import io.github.mortuusars.exposure.camera.viewfinder.ZoomDirection;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Exposure.ID, value = Dist.CLIENT)
public class ViewfinderRenderer {
    private static final ResourceLocation VIEWFINDER_TEXTURE = new ResourceLocation("exposure:textures/misc/viewfinder.png");
    private static final PoseStack POSE_STACK = new PoseStack();

    private static final float MAX_FOV = Fov.focalLengthToFov(18);
    private static final float MIN_FOV = Fov.focalLengthToFov(200);

    private static Camera.FocalRange focalRange = Camera.FocalRange.DEFAULT;
    private static double defaultFov = -1;
    private static double currentFov = -1;
    private static double targetFov = -1;
    private static boolean fovRestored;

    public static boolean shouldRender() {
        return Camera.getViewfinder().isActive(Minecraft.getInstance().player);
    }

    public static void render() {
        int color = 0xfa1f1d1b;
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        float finderSize = (float)Math.min(width, height);
        float openingStartX = (width - finderSize) / 2f - 1;
        float openingStartY = (height - finderSize) / 2f;
        float openingEndX = openingStartX + finderSize;
        float openingEndY = openingStartY + finderSize;

        PoseStack poseStack = POSE_STACK;

        poseStack.pushPose();
        poseStack.translate(0, 0, 1000);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Left
        GuiComponent.fill(poseStack, 0, (int)openingStartY, (int)openingStartX, (int)openingEndY, color);
        // Right
        GuiComponent.fill(poseStack, (int)openingEndX, (int)openingStartY, width, (int)openingEndY, color);
        // Top
        GuiComponent.fill(poseStack, 0, 0, width, (int)openingStartY, color);
        // Bottom
        GuiComponent.fill(poseStack, 0, (int)openingEndY, width, height, color);

        // TODO: Shutter
//        if (!Camera.isCapturing() && Minecraft.getInstance().player.getCooldowns().isOnCooldown(cameraStack.getItem())) {
//            GuiComponent.fill(poseStack, (int) openingStartX, (int)openingStartY, (int) openingEndX, (int) openingEndY, color);
//        }

        // Texture overlay
        if (!Minecraft.getInstance().options.hideGui) {
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VIEWFINDER_TEXTURE);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();

            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(openingStartX, openingEndY, -90).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(openingEndX, openingEndY, -90).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(openingEndX, openingStartY, -90).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(openingStartX, openingStartY, -90).uv(0.0F, 0.0F).endVertex();
            tesselator.end();
        }

        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    public static double modifyMouseSensitivity(double sensitivity) {
        if (!shouldRender())
            return sensitivity;

        //TODO: config on/off sens, and multiplier
        // TODO: Should take lens focal ranges into account
        return sensitivity * Mth.clamp(1f - (MAX_FOV - currentFov) / MAX_FOV, 0.1f, 1f);
    }

    @SubscribeEvent
    public static void computeFov(ViewportEvent.ComputeFov event) {
        if (event.usedConfiguredFov()) {
            defaultFov = event.getFOV();
            if (targetFov == -1)
                targetFov = defaultFov;
            if (currentFov == -1)
                currentFov = defaultFov;
        }

        if (shouldRender()) {
            currentFov += (targetFov - currentFov) * 0.025 * event.getPartialTick();
            fovRestored = false;
        }
        else if (!fovRestored) {
            currentFov += (defaultFov - currentFov) * 0.125 * event.getPartialTick();
            if (Math.abs(currentFov - defaultFov) < 0.001d)
                fovRestored = true;
        }
        else {
            currentFov = defaultFov;
            return;
        }

        event.setFOV(currentFov);
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (shouldRender() && event.getScrollDelta() != 0) {
            event.setCanceled(true);

            ZoomDirection direction = event.getScrollDelta() < 0d ? ZoomDirection.IN : ZoomDirection.OUT;

            double step = 8d * ( 1f - Mth.clamp((MAX_FOV - currentFov) / MAX_FOV, 0.3f, 1f));
            double inertia = (float)Math.abs((targetFov - currentFov)) * 0.8f;
            double change = step + inertia;
            change = (float) change;

            if (Screen.hasControlDown())
                change *= 0.25f;

            targetFov = Mth.clamp(targetFov += direction == ZoomDirection.IN ? +change : -change,
                    Fov.focalLengthToFov(focalRange.max()),
                    Fov.focalLengthToFov(focalRange.min()));
        }
    }

    public static void update() {
        focalRange = CameraItem.getCameraInHand(Minecraft.getInstance().player)
                .map(itemAndStack -> itemAndStack.getItem().getFocalRange(itemAndStack.getStack()))
                .orElse(Camera.FocalRange.DEFAULT);

        Exposure.LOGGER.info(focalRange.toString());

        targetFov = Mth.clamp(targetFov, Fov.focalLengthToFov(focalRange.max()), Fov.focalLengthToFov(focalRange.min()));
    }
}
