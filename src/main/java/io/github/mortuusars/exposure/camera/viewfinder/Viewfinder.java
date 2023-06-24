package io.github.mortuusars.exposure.camera.viewfinder;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class Viewfinder {
    public static final String FOCAL_LENGTH_TAG = "FocalLength";

    private static final ResourceLocation VIEWFINDER_TEXTURE = new ResourceLocation("exposure:textures/misc/viewfinder.png");

    private static float maxFov = focalLengthToFov(18);
    private static float minFov = focalLengthToFov(55);

    public static float currentFov;
    public static float targetFov = maxFov;

    private static boolean isActive;
    private static ItemStack cameraStack;
    private static InteractionHand holdingHand;

    public static void activate(ItemStack camera, InteractionHand hand) {
        Preconditions.checkArgument(!camera.isEmpty(), "Camera cannot be empty.");
        setActive(true, camera, hand);
    }

    public static void deactivate() {
        setActive(false, cameraStack, holdingHand);
    }

    @SuppressWarnings("DataFlowIssue")
    private static void setActive(boolean active, ItemStack camera, InteractionHand hand) {
        isActive = active;
        cameraStack = camera;
        holdingHand = hand;

        MinMaxBounds<Integer> focalRange = ((CameraItem) cameraStack.getItem()).getFocalRange(cameraStack);

        maxFov = focalLengthToFov(focalRange.getMin());
        minFov = focalLengthToFov(focalRange.getMax());

        if (cameraStack.getTag() != null && camera.getTag().contains(FOCAL_LENGTH_TAG, Tag.TAG_INT)) {
            int focalLength = cameraStack.getOrCreateTag().getInt(FOCAL_LENGTH_TAG);
            float fov = focalLengthToFov(focalLength);

            targetFov = Mth.clamp(fov, minFov, maxFov);
        }
    }

    public static boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        if (isActive && mc.player != null) {
            isActive = mc.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof CameraItem
                    || mc.player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof CameraItem;
        }

        return isActive;
    }

    public static double getMouseSensitivityModifier() {
        // TODO: Should take lens focal ranges into account
        return isActive ? Mth.clamp(1f - (maxFov - targetFov) / maxFov, 0.1f, 1f) : 1f;
    }

    public static void renderOverlay(PoseStack poseStack, float partialTicks) {
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int color = 0xfa1f1d1b;

        poseStack.pushPose();
        poseStack.translate(0, 0, 1000);

        float finderSize = (float)Math.min(width, height);

        float openingStartX = (width - finderSize) / 2f - 1;
        float openingStartY = (height - finderSize) / 2f;
        float openingEndX = openingStartX + finderSize;
        float openingEndY = openingStartY + finderSize;

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
        if (!Camera.isCapturing() && Minecraft.getInstance().player.getCooldowns().isOnCooldown(cameraStack.getItem())) {
            GuiComponent.fill(poseStack, (int) openingStartX, (int)openingStartY, (int) openingEndX, (int) openingEndY, color);
        }

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

    public static int fovToFocalLength(float fov) {
        double sensorWidth = 36.0; // Sensor width in millimeters
        return (int) Math.round(sensorWidth / (2.0 * Math.tan(Math.toRadians(fov / 2.0))));
    }

    public static float focalLengthToFov(int focalLength) {
        double sensorWidth = 36.0; // Sensor width in millimeters
        return (float) (2.0 * Math.toDegrees(Math.atan(sensorWidth / (2.0 * focalLength))));
    }

    public static void modifyZoom(ZoomDirection direction) {
        float step = 10f * ( 1f - Mth.clamp((maxFov - currentFov) / maxFov, 0.3f, 1f));
        float inertia = Math.abs((targetFov - currentFov)) * 0.8f;

        float change = step + inertia;

        if (Screen.hasControlDown())
            change *= 0.25f;

        targetFov = Mth.clamp(targetFov += direction == ZoomDirection.IN ? +change : -change, minFov, maxFov);

        cameraStack.getOrCreateTag().putInt(FOCAL_LENGTH_TAG, fovToFocalLength(targetFov));
        ((CameraItem) cameraStack.getItem()).clientsideUpdateCameraInInventory(cameraStack, holdingHand);
    }
}
