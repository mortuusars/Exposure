package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import io.github.mortuusars.exposure.world.item.BrokenInterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.exposure.util.Rect2f;
import io.github.mortuusars.mortaar.util.Easing;
import net.minecraft.SharedConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ViewfinderOverlay {
    public static final ResourceLocation VIEWFINDER_TEXTURE = Exposure.resource("textures/gui/viewfinder/viewfinder.png");
    public static final ResourceLocation NO_FILM_ICON_TEXTURE = Exposure.resource("textures/gui/viewfinder/no_film.png");
    public static final ResourceLocation REMAINING_FRAMES_ICON_TEXTURE = Exposure.resource("textures/gui/viewfinder/remaining_frames.png");
    public static final ResourceLocation BSOD_SAD_FACE_TEXTURE = Exposure.resource("textures/gui/viewfinder/bsod_sad_face.png");
    public static final ResourceLocation BSOD_QR_CODE_TEXTURE = Exposure.resource("textures/gui/viewfinder/bsod_qr_code.png");

    protected final LocalPlayer player;
    protected final Camera camera;
    protected final Viewfinder viewfinder;
    protected final Rect2f opening;

    protected final Animation scaleAnimation;
    protected final float initialScale;

    protected int backgroundColor;

    protected float scale;
    protected float bobX = 0f;
    protected float bobY = 0f;
    protected float attackAnim = 0f;
    protected float xRot;
    protected float yRot;
    protected float xRot0;
    protected float yRot0;

    protected boolean forceDrawShutterOnNextFrame;
    protected long forceDrawShutterUntil = -1;

    public ViewfinderOverlay(Camera camera, Viewfinder viewfinder) {
        this.player = Minecrft.playerOrThrow();
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.backgroundColor = Config.getColor(Config.Client.VIEWFINDER_BACKGROUND_COLOR);
        this.opening = new Rect2f(0, 0, 0, 0);
        recalculateOpening();

        this.scaleAnimation = new Animation(300, Easing.OUT_EXPO);
        this.initialScale = 0.5f;
        this.scale = 0.5f; // Start small to animate expanding

        this.xRot = Minecrft.get().gameRenderer.getMainCamera().getXRot();
        this.yRot = Minecrft.get().gameRenderer.getMainCamera().getYRot();
        this.xRot0 = xRot;
        this.yRot0 = yRot;
    }

    public Rect2f getOpening() {
        return opening;
    }

    public float getScale() {
        return scale;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        recalculateOpening();
        scale = Mth.lerp((float) scaleAnimation.getValue(), initialScale, 1f);

        // opening and scale is updated even if overlay is not rendered - other classes may depend on them.

        if (!viewfinder.isLookingThrough() || Minecrft.options().hideGui || camera.isEmpty()) return;

        final int width = Minecrft.get().getWindow().getGuiScaledWidth();
        final int height = Minecrft.get().getWindow().getGuiScaledHeight();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(width / 2f, height / 2f, 0);
        guiGraphics.pose().scale(scale, scale, scale);

        if (Minecrft.options().bobView().get()) {
            bobView(guiGraphics.pose(), deltaTracker);
        }
        applyAttackAnimation(guiGraphics.pose(), deltaTracker);
        applyMovementDelay(guiGraphics.pose(), deltaTracker);

        guiGraphics.pose().translate(-width / 2f, -height / 2f, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // -9999 to cover all screen when overlay is scaled down
        // Left
        GuiUtil.drawRect(guiGraphics, opening.x, opening.y, -9999, opening.height, backgroundColor);
        // Right
        GuiUtil.drawRect(guiGraphics, opening.x + opening.width, opening.y, 9999, opening.height, backgroundColor);
        // Top
        GuiUtil.drawRect(guiGraphics, -4999, opening.y, 9999, -9999, backgroundColor);
        // Bottom
        GuiUtil.drawRect(guiGraphics, -4999, opening.y + opening.height, 9999, 9999, backgroundColor);

        boolean drawGuide = true;

        StoredItemStack filter = Attachment.FILTER.get(camera.getItemStack());
        if (filter.getForReading().getItem() instanceof BrokenInterplanarProjectorItem brokenInterplanarProjector) {
            drawGuide = false;
            renderBSOD(guiGraphics, brokenInterplanarProjector, filter.getForReading());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        drawShutter(guiGraphics);
        drawViewfinderTexture(guiGraphics);

        // Guide
        if (drawGuide) {
            ResourceLocation guideTexture = CameraSettings.COMPOSITION_GUIDE.getOrDefault(camera.getItemStack()).overlayTextureLocation();
            GuiUtil.blit(guideTexture, guiGraphics.pose(), opening, 0, 0, (int) opening.width, (int) opening.height, 0);
        }

        if (!(Minecrft.get().screen instanceof ViewfinderCameraControlsScreen)) {
            renderStatusIcons(guiGraphics.pose(), camera.getItemStack());
        }

        guiGraphics.pose().popPose();
        RenderSystem.disableDepthTest();
    }

    protected void drawViewfinderTexture(GuiGraphics guiGraphics) {
        GuiUtil.blit(VIEWFINDER_TEXTURE, guiGraphics.pose(), opening, 0, 0, (int) opening.width, (int) opening.height, 0);
    }

    /**
     * Usually shutter drawing is controlled by camera#isShutterOpen,
     * but due to fast shutter speeds and occasional lag, shutter may not be drawn at all.
     * So we force shutter to render for at least some time when shutter is open. <br><br>
     * Combination of timestamp and next frame check is for cases where lag takes so long that 'forceDrawShutterUntil' is passed without rendering.
     */
    protected void drawShutter(GuiGraphics guiGraphics) {
        if (camera.isShutterOpen() || forceDrawShutterOnNextFrame || forceDrawShutterUntil - System.currentTimeMillis() > 0) {
            GuiUtil.drawRect(guiGraphics, opening, 0xfa1f1d1b);
            forceDrawShutterOnNextFrame = false;
        }
    }

    public void startDrawingShutter() {
        forceDrawShutterOnNextFrame = true;
        forceDrawShutterUntil = UnixTimestamp.Milliseconds.now() + SharedConstants.MILLIS_PER_TICK * 2;
    }

    protected void renderBSOD(GuiGraphics guiGraphics, BrokenInterplanarProjectorItem item, ItemStack stack) {
        Font font = Minecrft.get().font;

        int yCenter = (int) (opening.y + opening.height / 2);
        int margin = font.lineHeight;

        int x = (int) (opening.x + opening.width * 0.125f);
        int y = yCenter;

        int sadFaceSize = font.lineHeight * 5;
        guiGraphics.blit(BSOD_SAD_FACE_TEXTURE, x, y - sadFaceSize - margin, 0, 0, sadFaceSize, sadFaceSize, sadFaceSize, sadFaceSize);

        MutableComponent message = Component.translatable("item.exposure.broken_interplanar_projector.viewfinder.message");
        List<FormattedCharSequence> messageLines = font.split(message, (int) (opening.width * 0.75f));

        for (FormattedCharSequence line : messageLines) {
            guiGraphics.drawString(font, line, x, y, 0xFFFFFFFF, false);
            y += font.lineHeight;
        }

        y += margin;

        int qrCodeTextureSize = 41;
        int qrCodeSize = switch ((int) Minecrft.get().getWindow().getGuiScale()) {
            case 1 -> qrCodeTextureSize * 3;
            case 2 -> qrCodeTextureSize * 2;
            default -> qrCodeTextureSize;
        };
        guiGraphics.blit(BSOD_QR_CODE_TEXTURE, x, y, 0, 0, qrCodeSize, qrCodeSize, qrCodeSize, qrCodeSize);

        MutableComponent errorCode = Component.translatable("item.exposure.broken_interplanar_projector.viewfinder.error_code");
        guiGraphics.drawString(font, errorCode, x + qrCodeSize + margin, y, 0xFFFFFFFF, false);
        y += font.lineHeight;
        String code = item.getErrorCode(stack);
        guiGraphics.drawString(font, code, x + qrCodeSize + margin, y, 0xFFFFFFFF, false);
    }

    public void bobView(PoseStack poseStack, DeltaTracker deltaTracker) {
        if (Minecrft.get().getCameraEntity() instanceof Player pl) {
            float walkDist = Mth.lerp(deltaTracker.getGameTimeDeltaTicks(), pl.walkDistO, pl.walkDist);
            float strength = Mth.lerp(deltaTracker.getGameTimeDeltaTicks(), pl.oBob, pl.bob);
            float x = Mth.sin(walkDist * (float) Math.PI) * strength;
            float y = Math.abs(Mth.cos(walkDist * (float) Math.PI) * strength);

            float delta = Math.min(deltaTracker.getGameTimeDeltaTicks(), 1f);
            bobX = Mth.lerp(delta, bobX, x);
            bobY = Mth.lerp(delta, bobY, y);
            double guiScale = Minecrft.get().getWindow().getGuiScale();
            poseStack.translate(bobX * 100 / guiScale, bobY * 200 / guiScale, 0.0F);
            float scale = this.scale - (bobY * 0.25f);
            poseStack.scale(scale, scale, scale);
        }
    }

    public void applyAttackAnimation(PoseStack poseStack, DeltaTracker deltaTracker) {
        float attack = player.attackAnim;
        if (attack > 0.1f)
            attack = 1f - attack;

        float delta = Math.min(deltaTracker.getGameTimeDeltaTicks(), 1f);
        attackAnim = Mth.lerp(delta, attackAnim, attack);

        poseStack.scale(1f - attackAnim * 0.1f, 1f - attackAnim * 0.2f, 1f - attackAnim * 0.1f);
        poseStack.mulPose(Axis.ZN.rotationDegrees(Mth.lerp(attackAnim, 0, 5)));
        double guiScale = Minecrft.get().getWindow().getGuiScale();
        poseStack.translate(0, 60f / guiScale * attackAnim, 0);
    }

    public void applyMovementDelay(PoseStack poseStack, DeltaTracker deltaTracker) {
        float delta = Math.min(deltaTracker.getGameTimeDeltaTicks() * 0.6f, 1.0f);
        xRot0 = Mth.lerp(delta, xRot0, xRot);
        yRot0 = Mth.lerp(delta, yRot0, yRot);
        xRot = Minecrft.get().gameRenderer.getMainCamera().getXRot();
        yRot = Minecrft.get().gameRenderer.getMainCamera().getYRot();
        double guiScale = Minecrft.get().getWindow().getGuiScale();
        double horizontalDelay = (yRot - yRot0) / guiScale * 3;
        double verticalDelay = (xRot - xRot0) / guiScale * 3;
        poseStack.translate(-horizontalDelay, -verticalDelay, 0);
    }

    protected void recalculateOpening() {
        final int width = Minecrft.get().getWindow().getGuiScaledWidth();
        final int height = Minecrft.get().getWindow().getGuiScaledHeight();
        final float openingSize = Math.min(width, height);

        opening.x = (width - openingSize) / 2f;
        opening.y = (height - openingSize) / 2f;
        opening.width = openingSize;
        opening.height = openingSize;
    }

    protected void renderStatusIcons(PoseStack poseStack, ItemStack cameraStack) {
        ItemStack filmStack = Attachment.FILM.get(cameraStack).getForReading();

        if (filmStack.isEmpty()
                || !(filmStack.getItem() instanceof FilmRollItem filmRollItem)
                || !filmRollItem.canAddFrame(filmStack)) {
            renderNoFilmIcon(poseStack);
            return;
        }

        renderRemainingFramesIcon(poseStack, filmRollItem, filmStack);
    }

    protected void renderNoFilmIcon(PoseStack poseStack) {
        RenderSystem.setShaderTexture(0, NO_FILM_ICON_TEXTURE);
        int x = (int) ((opening.x + (opening.width / 2) - 12)) + Config.Client.VIEWFINDER_STATUS_ICON_OFFSET_X.get();
        int y = (int) (opening.y + opening.height - 18) + Config.Client.VIEWFINDER_STATUS_ICON_OFFSET_Y.get();
        GuiUtil.blit(poseStack, x, y, 23, 18, 0, 0, 23, 18, 0);
    }

    protected void renderRemainingFramesIcon(PoseStack poseStack, FilmRollItem filmRollItem, ItemStack filmStack) {
        int maxFrames = filmRollItem.getMaxFrameCount(filmStack);
        int exposedFrames = filmRollItem.getStoredFramesCount(filmStack);
        int remainingFrames = Math.max(0, maxFrames - exposedFrames);
        if (maxFrames > 5 && remainingFrames <= 3) {
            RenderSystem.setShaderTexture(0, REMAINING_FRAMES_ICON_TEXTURE);
            float x = (int) (opening.x + (opening.width / 2) - 17) + Config.Client.VIEWFINDER_STATUS_ICON_OFFSET_X.get();
            float y = (int) (opening.y + opening.height - 15) + Config.Client.VIEWFINDER_STATUS_ICON_OFFSET_Y.get();
            int vOffset = (remainingFrames - 1) * 15;
            GuiUtil.blit(poseStack, x, y, 33, 15, 0, vOffset, 33, 45, 0);
        }
    }
}
