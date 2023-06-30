package io.github.mortuusars.exposure.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Camera;
import io.github.mortuusars.exposure.camera.CameraCapture;
import io.github.mortuusars.exposure.camera.viewfinder.ZoomDirection;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.Fov;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.geom.Rectangle2D;
import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Exposure.ID, value = Dist.CLIENT)
public class ViewfinderRenderer {
    private static final ResourceLocation VIEWFINDER_TEXTURE = new ResourceLocation("exposure:textures/misc/viewfinder.png");
    private static final PoseStack POSE_STACK = new PoseStack();

    private static final float MAX_FOV = Fov.focalLengthToFov(18);
//    private static final float MIN_FOV = Fov.focalLengthToFov(200);

    private static Camera.FocalRange focalRange = Camera.FocalRange.DEFAULT;
    private static double defaultFov = -1;
    private static double currentFov = -1;
    private static double targetFov = -1;
    public static boolean fovRestored;

    public static long openingAnimationStartTime = -1;
    public static long currentTimestamp;
    public static float openingAnimationProgress = 0f;
    public static float openingAnimationDuration = 350f;

    public static Rectangle2D.Float opening;
    public static float targetOpeningSize;
    public static float openingSize;

    public static double getCurrentFov() {
        return currentFov;
    }

    public static void setup(Player player, InteractionHand hand) {
        if (player != Minecraft.getInstance().player)
            return;

        focalRange = CameraItem.getCameraInHand(Minecraft.getInstance().player)
                .map(itemAndStack -> itemAndStack.getItem().getFocalRange(itemAndStack.getStack()))
                .orElse(Camera.FocalRange.DEFAULT);

        targetFov = Mth.clamp(targetFov, Fov.focalLengthToFov(focalRange.max()), Fov.focalLengthToFov(focalRange.min()));

        targetOpeningSize = Math.min(Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
        openingSize = targetOpeningSize - targetOpeningSize * 0.6f;

        openingAnimationProgress = 0f;
        openingAnimationStartTime = System.currentTimeMillis();
    }

    public static boolean shouldRender() {
        return Camera.getViewfinder().isActive(Minecraft.getInstance().player);
    }

    public static void render() {
        currentTimestamp = System.currentTimeMillis();
        openingAnimationProgress = Mth.clamp((currentTimestamp - openingAnimationStartTime) / 350f, 0.0f, 1.0f);

        int color = 0xfa1f1d1b; //TODO: configurable colors.
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

//        float rAnim = (float) (1 - Math.pow(1 - openingAnimationProgress, 3));;
//        rAnim = 1f - rAnim;
//
//        double fovMagnitude = (MAX_FOV - targetFov) / MAX_FOV;
//
//        float anim = (float) (rAnim * (height * 0.5f * (fovMagnitude + 0.1f)));
//        float openingSize = Math.min(width, height) - anim * 2;

        openingSize = Mth.lerp((0.5f /*+ openingSize * 0.0025f*/) * Minecraft.getInstance().getDeltaFrameTime(), openingSize, targetOpeningSize);
//        openingSize += (targetOpeningSize - openingSize) * 0.25f * Minecraft.getInstance().getDeltaFrameTime();

        opening = new Rectangle2D.Float((width - openingSize) / 2f, (height - openingSize) / 2f, openingSize, openingSize);

        PoseStack poseStack = POSE_STACK;

        // Left
        drawRect(poseStack, 0, opening.y, opening.x, opening.y + opening.height, color);
        // Right
        drawRect(poseStack, opening.x + opening.width, opening.y, width, opening.y + opening.height, color);
        // Top
        drawRect(poseStack, 0, 0, width, opening.y, color);
        // Bottom
        drawRect(poseStack, 0, opening.y + opening.height, width, height, color);

        Optional<ItemAndStack<CameraItem>> cameraInHand = CameraItem.getCameraInHand(Minecraft.getInstance().player);

        // TODO: Shutter
        if (!CameraCapture.isCapturing() && cameraInHand.isPresent()
                && Minecraft.getInstance().player.getCooldowns().isOnCooldown(cameraInHand.get().getItem())) {
            drawRect(poseStack, opening.x, opening.y, opening.x + opening.width, opening.y + opening.height, color);
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
            bufferbuilder.vertex(opening.x, opening.y + opening.height, -90).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(opening.x + opening.width, opening.y + opening.height, -90).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(opening.x + opening.width, opening.y, -90).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(opening.x, opening.y, -90).uv(0.0F, 0.0F).endVertex();
            tesselator.end();
        }
    }

    public static void drawRect(PoseStack poseStack, float minX, float minY, float maxX, float maxY, int color) {
        if (minX < maxX) {
            float temp = minX;
            minX = maxX;
            maxX = temp;
        }

        if (minY < maxY) {
            float temp = minY;
            minY = maxY;
            maxY = temp;
        }

        float alpha = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix, minX, maxY, 0.0F).color(r, g, b, alpha).endVertex();
        bufferbuilder.vertex(matrix, maxX, maxY, 0.0F).color(r, g, b, alpha).endVertex();
        bufferbuilder.vertex(matrix, maxX, minY, 0.0F).color(r, g, b, alpha).endVertex();
        bufferbuilder.vertex(matrix, minX, minY, 0.0F).color(r, g, b, alpha).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static double modifyMouseSensitivity(double sensitivity) {
        if (!shouldRender())
            return sensitivity;

        //TODO: config on/off sens, and multiplier
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
            currentFov = Mth.lerp((0.25f) * Minecraft.getInstance().getDeltaFrameTime(), currentFov, targetFov);
            fovRestored = false;
        }
        else if (!fovRestored) {
            currentFov = Mth.lerp((0.5f) * Minecraft.getInstance().getDeltaFrameTime(), currentFov, defaultFov);

            if (Math.abs(currentFov - defaultFov) < 0.0001d) {
                fovRestored = true;

                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null && event.usedConfiguredFov()) {
                    // Item in hand snaps weirdly when fov is changing to normal.
                    // So we render hand only after fov is restored and play equip animation to smoothly show a hand.
                    Minecraft.getInstance().gameRenderer.itemInHandRenderer.itemUsed(player.getItemInHand(
                            InteractionHand.MAIN_HAND).isEmpty() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                }
            }
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
            zoom(event.getScrollDelta() < 0d ? ZoomDirection.IN : ZoomDirection.OUT, false);
        }
    }

    public static void zoom(ZoomDirection direction, boolean precise) {
        double step = 8d * ( 1f - Mth.clamp((MAX_FOV - currentFov) / MAX_FOV, 0.3f, 1f));
        double inertia = (float)Math.abs((targetFov - currentFov)) * 0.8f;
        double change = step + inertia;
        change = (float) change;

        if (precise)
            change *= 0.25f;

        targetFov = Mth.clamp(targetFov += direction == ZoomDirection.IN ? +change : -change,
                Fov.focalLengthToFov(focalRange.max()),
                Fov.focalLengthToFov(focalRange.min()));

        //TODO: Save focal length to camera stack
    }

    public static void update() {
        focalRange = CameraItem.getCameraInHand(Minecraft.getInstance().player)
                .map(itemAndStack -> itemAndStack.getItem().getFocalRange(itemAndStack.getStack()))
                .orElse(Camera.FocalRange.DEFAULT);

        targetFov = Mth.clamp(targetFov, Fov.focalLengthToFov(focalRange.max()), Fov.focalLengthToFov(focalRange.min()));
    }
}
