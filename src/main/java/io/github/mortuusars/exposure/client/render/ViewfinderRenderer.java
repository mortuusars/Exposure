package io.github.mortuusars.exposure.client.render;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.SynchronizedCameraInHandActions;
import io.github.mortuusars.exposure.camera.component.FocalRange;
import io.github.mortuusars.exposure.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;

import java.awt.geom.Rectangle2D;

public class ViewfinderRenderer {
    private static final ResourceLocation VIEWFINDER_TEXTURE = Exposure.resource("textures/misc/viewfinder.png");
    private static final PoseStack POSE_STACK = new PoseStack();

    public static Rectangle2D.Float opening;

    private static Minecraft minecraft = Minecraft.getInstance();
    private static Player player = minecraft.player;

    private static float scale = 1f;

    private static FocalRange focalRange = FocalRange.FULL;
    private static float defaultFov = -1;
    private static float currentFov = -1;
    private static float targetFov = -1;
    public static boolean fovRestored;

    public static float getCurrentFov() {
        return currentFov;
    }

    public static void setup() {
        minecraft = Minecraft.getInstance();
        player = minecraft.player;

        Preconditions.checkState(player != null, "Player should not be null");

        CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
        Preconditions.checkState(!camera.isEmpty(), "Player must be holding a camera.");

        focalRange = camera.getItem().getFocalRange(camera.getStack());
        targetFov = Fov.focalLengthToFov( Mth.clamp(camera.getItem().getZoom(camera.getStack()), focalRange.min(), focalRange.max()));
        fovRestored = false;
        scale = 0.5f;
    }

    public static float getScale() {
        return scale;
    }

    public static boolean shouldRender() {
        return Exposure.getCamera().isActive(Minecraft.getInstance().player)
                && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    public static void render() {
        int color = 0xfa1f1d1b; //TODO: configurable colors.
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        scale = Mth.lerp(0.5f * minecraft.getDeltaFrameTime(), scale, 1f);
        float openingSize = Math.min(width, height);
        opening = new Rectangle2D.Float((width - openingSize) / 2f, (height - openingSize) / 2f, openingSize, openingSize);

        if (!minecraft.options.hideGui) {
            PoseStack poseStack = POSE_STACK;
            poseStack.pushPose();
            poseStack.translate(width / 2f, height / 2f, 0);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-width / 2f, -height / 2f, 0);

            if (Minecraft.getInstance().options.bobView().get())
                bobView(poseStack, Minecraft.getInstance().getPartialTick());

            // -999 to cover all screen when poseStack is scaled down.
            // Left
            drawRect(poseStack, -999, opening.y, opening.x, opening.y + opening.height, color);
            // Right
            drawRect(poseStack, opening.x + opening.width, opening.y, width + 999, opening.y + opening.height, color);
            // Top
            drawRect(poseStack, -999, -999, width + 999, opening.y, color);
            // Bottom
            drawRect(poseStack, -999, opening.y + opening.height, width + 999, height + 999, color);

            // Shutter
            if (Exposure.getCamera().getShutter().isOpen(player))
                drawRect(poseStack, opening.x, opening.y, opening.x + opening.width, opening.y + opening.height, color);

            // Opening Texture
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VIEWFINDER_TEXTURE);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();

            Matrix4f matrix = poseStack.last().pose();

            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(matrix, opening.x, opening.y + opening.height, 0).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(matrix, opening.x + opening.width, opening.y + opening.height, 0).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(matrix, opening.x + opening.width, opening.y, 0).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(matrix, opening.x, opening.y, 0).uv(0.0F, 0.0F).endVertex();
            tesselator.end();

            // Guide
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
            RenderSystem.setShaderTexture(0, camera.getItem().getCompositionGuide(camera.getStack()).getTexture());

            tesselator = Tesselator.getInstance();
            bufferbuilder = tesselator.getBuilder();

            matrix = poseStack.last().pose();

            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(matrix, opening.x, opening.y + opening.height, -90).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(matrix, opening.x + opening.width, opening.y + opening.height, -90).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(matrix, opening.x + opening.width, opening.y, -90).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(matrix, opening.x, opening.y, -90).uv(0.0F, 0.0F).endVertex();
            tesselator.end();

            poseStack.popPose();
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

    private static void bobView(PoseStack pMatrixStack, float pPartialTicks) {
        if (Minecraft.getInstance().getCameraEntity() instanceof Player) {
            Player player = (Player)Minecraft.getInstance().getCameraEntity();
            float f = player.walkDist - player.walkDistO;
            float f1 = -(player.walkDist + f * pPartialTicks);
            float f2 = Mth.lerp(pPartialTicks, player.oBob, player.bob);
            pMatrixStack.translate((Mth.sin(f1 * (float)Math.PI) * f2 * 16F), (-Math.abs(Mth.cos(f1 * (float)Math.PI) * f2 * 32F)), 0.0D);
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(f1 * (float)Math.PI) * f2 * 3.0F));
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F));
        }
    }

    public static double modifyMouseSensitivity(double sensitivity) {
        if (!shouldRender())
            return sensitivity;

        //TODO: config on/off sens, and multiplier
        return sensitivity * Mth.clamp(1f - (defaultFov - currentFov) / defaultFov, 0.1f, 1f);
    }

    public static void onComputeFovEvent(ViewportEvent.ComputeFov event) {
        if (event.usedConfiguredFov()) {
            defaultFov = (float) event.getFOV();
            if (targetFov == -1)
                targetFov = defaultFov;
            if (currentFov == -1)
                currentFov = defaultFov;
        }

        if (shouldRender()) {
            currentFov = Mth.lerp((0.25f) * minecraft.getDeltaFrameTime(), currentFov, targetFov);
        }
        else if (!fovRestored) {
            currentFov = Mth.lerp((0.5f) * minecraft.getDeltaFrameTime(), currentFov, defaultFov);

            if (Math.abs(currentFov - defaultFov) < 0.0001d) {
                fovRestored = true;

                LocalPlayer player = minecraft.player;
                if (player != null && event.usedConfiguredFov()) {
                    // Item in hand snaps weirdly when fov is changing to normal.
                    // So we render hand only after fov is restored and play equip animation to smoothly show a hand.
                    minecraft.gameRenderer.itemInHandRenderer.itemUsed(player.getItemInHand(
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

    public static void onMouseScrollEvent(InputEvent.MouseScrollingEvent event) {
        if (shouldRender() && event.getScrollDelta() != 0) {
            event.setCanceled(true);
            zoom(event.getScrollDelta() > 0d ? ZoomDirection.IN : ZoomDirection.OUT, false);
        }
    }

    public static void zoom(ZoomDirection direction, boolean precise) {
        float step = (float) (8d * ( 1f - Mth.clamp((focalRange.min() - currentFov) / focalRange.min(), 0.3f, 1f)));
        float inertia = Math.abs((targetFov - currentFov)) * 0.8f;
        float change = step + inertia;

        if (precise)
            change *= 0.25f;


        float prevFov = targetFov;

        float fov = Mth.clamp(targetFov + (direction == ZoomDirection.IN ? -change : +change),
                Fov.focalLengthToFov(focalRange.max()),
                Fov.focalLengthToFov(focalRange.min()));

        if (Math.abs(prevFov - fov) > 0.01f)
            player.playSound(SoundEvents.SPYGLASS_STOP_USING);

        targetFov = fov;
        SynchronizedCameraInHandActions.setZoom(Fov.fovToFocalLength(fov));
    }
}
