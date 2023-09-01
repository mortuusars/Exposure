package io.github.mortuusars.exposure.client.renderer;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.component.FocalRange;
import io.github.mortuusars.exposure.camera.component.ZoomDirection;
import io.github.mortuusars.exposure.camera.infrastructure.SynchronizedCameraInHandActions;
import io.github.mortuusars.exposure.client.gui.screen.ViewfinderControlsScreen;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.Fov;
import io.github.mortuusars.exposure.util.GuiUtil;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Rectangle2D;
import java.util.Optional;

public class ViewfinderRenderer {
    private static final ResourceLocation VIEWFINDER_TEXTURE = Exposure.resource("textures/gui/viewfinder/viewfinder.png");
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

    private static float xRot = 0f;
    private static float yRot = 0f;
    private static float xRot0 = 0f;
    private static float yRot0 = 0f;
    private static int backgroundColor;

    public static float getCurrentFov() {
        return currentFov;
    }

    @Nullable
    private static String previousEffect;

    public static void setup() {
        minecraft = Minecraft.getInstance();
        player = minecraft.player;
        assert player != null;

        backgroundColor = Config.Client.getBackgroundColor();

        xRot = player.getXRot();
        yRot = player.getYRot();
        xRot0 = xRot;
        yRot0 = yRot;

        Preconditions.checkState(player != null, "Player should not be null");

        CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
        Preconditions.checkState(!camera.isEmpty(), "Player must be holding a camera.");

        focalRange = camera.getItem().getFocalRange(camera.getStack());
        targetFov = Fov.focalLengthToFov( Mth.clamp(camera.getItem().getZoom(camera.getStack()), focalRange.min(), focalRange.max()));
        fovRestored = false;
        scale = 0.5f;

        Optional<ItemStack> attachment = camera.getItem().getAttachment(camera.getStack(), CameraItem.FILTER_ATTACHMENT);
        attachment.ifPresent(stack -> {
            @Nullable String shader = null;
            if (stack.is(Items.GLASS_PANE))
                shader = "contrast";
            else if (stack.is(Tags.Items.GLASS_PANES)) {
                ResourceLocation itemLocation = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (itemLocation != null && itemLocation.getPath().contains("_stained_glass_pane")) {
                    String colorString = itemLocation.getPath().replace("_stained_glass_pane", "");
                    shader = colorString + "_tint";
                }
            }

            if (shader != null) {
                PostChain effect = Minecraft.getInstance().gameRenderer.currentEffect();
                if (effect != null)
                    previousEffect = effect.getName();

                Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation("exposure", "shaders/post/" + shader + ".json"));
            }
        });
    }

    public static void teardown() {
        Minecraft.getInstance().gameRenderer.shutdownEffect();

        if (previousEffect != null) {
            Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation(previousEffect));
            previousEffect = null;
        }
    }

    public static float getScale() {
        return scale;
    }

    public static boolean shouldRender() {
        return Exposure.getCamera().isActive(Minecraft.getInstance().player)
                && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    public static void render() {
        LocalPlayer player = Minecraft.getInstance().player;
        Preconditions.checkState(player != null && !Exposure.getCamera().getCameraInHand(player).isEmpty(),
                "Viewfinder overlay should not be rendered when player doesn't hold a camera.");

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        scale = Mth.lerp(0.5f * minecraft.getDeltaFrameTime(), scale, 1f);
        float openingSize = Math.min(width, height);


        opening = new Rectangle2D.Float((width - openingSize) / 2f, (height - openingSize) / 2f, openingSize, openingSize);

        if (!minecraft.options.hideGui) {
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            float delta = 0.75f * minecraft.getDeltaFrameTime();
            xRot0 = Mth.lerp(delta, xRot0, xRot);
            yRot0 = Mth.lerp(delta, yRot0, yRot);
            xRot = player.getXRot();
            yRot = player.getYRot();
            float xDelay = xRot - xRot0;
            float yDelay = yRot - yRot0;

            PoseStack poseStack = POSE_STACK;
            poseStack.pushPose();
            poseStack.translate(width / 2f, height / 2f, 0);
            poseStack.scale(scale, scale, scale);

            float attackAnim = Minecraft.getInstance().player.getAttackAnim(Minecraft.getInstance().getPartialTick());
            if (attackAnim > 0.5f)
                attackAnim = 1f - attackAnim;

            poseStack.scale(1f - attackAnim * 0.4f, 1f - attackAnim * 0.6f, 1f - attackAnim * 0.4f);
            poseStack.translate(width / 16f * attackAnim, width / 5f * attackAnim, 0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(attackAnim, 0, 10)));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(attackAnim, 0, 100)));

            poseStack.translate(-width / 2f - yDelay, -height / 2f - xDelay, 0);


            if (Minecraft.getInstance().options.bobView().get())
                bobView(poseStack, Minecraft.getInstance().getPartialTick());

            // -9999 to cover all screen when poseStack is scaled down.
            // Left
            drawRect(poseStack, -9999, opening.y, opening.x, opening.y + opening.height, backgroundColor);
            // Right
            drawRect(poseStack, opening.x + opening.width, opening.y, width + 9999, opening.y + opening.height, backgroundColor);
            // Top
            drawRect(poseStack, -9999, -9999, width + 9999, opening.y, backgroundColor);
            // Bottom
            drawRect(poseStack, -9999, opening.y + opening.height, width + 9999, height + 9999, backgroundColor);

            // Shutter
            if (Exposure.getCamera().getShutter().isOpen(ViewfinderRenderer.player))
                drawRect(poseStack, opening.x, opening.y, opening.x + opening.width, opening.y + opening.height, 0xfa1f1d1b);

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

            CameraInHand camera = Exposure.getCamera().getCameraInHand(ViewfinderRenderer.player);
            RenderSystem.setShaderTexture(0, Exposure.resource("textures/gui/viewfinder/composition_guide/" +
                    camera.getItem().getCompositionGuide(camera.getStack()).getId() + ".png"));

            tesselator = Tesselator.getInstance();
            bufferbuilder = tesselator.getBuilder();

            matrix = poseStack.last().pose();

            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(matrix, opening.x, opening.y + opening.height, -90).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(matrix, opening.x + opening.width, opening.y + opening.height, -90).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(matrix, opening.x + opening.width, opening.y, -90).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(matrix, opening.x, opening.y, -90).uv(0.0F, 0.0F).endVertex();
            tesselator.end();

            // Icons
            if (!(Minecraft.getInstance().screen instanceof ViewfinderControlsScreen)
                    && (camera.getItem().getFilm(camera.getStack()).isEmpty() || camera.getItem().getFilm(camera.getStack())
                        .map(flm -> flm.getItem().canAddFrame(flm.getStack())).orElse(false))) {
                RenderSystem.setShaderTexture(0, Exposure.resource("textures/gui/viewfinder/icon/no_film.png"));
                float cropFactor = Exposure.CROP_FACTOR;

                float fromEdge = (opening.height - (opening.height / (cropFactor))) / 2f;
                GuiUtil.blit(poseStack, (opening.x + (opening.width / 2) - 12), (opening.y + opening.height - ((fromEdge / 2 + 10))),
                        24, 19, 0, 0, 24, 19, 0);
            }

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

    public static void bobView(PoseStack pMatrixStack, float pPartialTicks) {
        if (Minecraft.getInstance().getCameraEntity() instanceof Player pl) {
            float f = pl.walkDist - pl.walkDistO;
            float f1 = -(pl.walkDist + f * pPartialTicks);
            float f2 = Mth.lerp(pPartialTicks, pl.oBob, pl.bob);
            pMatrixStack.translate((Mth.sin(f1 * (float)Math.PI) * f2 * 16F), (-Math.abs(Mth.cos(f1 * (float)Math.PI) * f2 * 32F)), 0.0D);
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(f1 * (float)Math.PI) * f2 * 3.0F));
        }
    }

    public static double modifyMouseSensitivity(double sensitivity) {
        if (!shouldRender())
            return sensitivity;

        double modifier = Mth.clamp(1f - (Config.Client.VIEWFINDER_ZOOM_SENSITIVITY_MODIFIER.get() * ((defaultFov - currentFov) / 5f)), 0.01, 2f);
        return sensitivity * modifier;
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
            player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get());

        targetFov = fov;
        SynchronizedCameraInHandActions.setZoom(Fov.fovToFocalLength(fov));
    }
}
