package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import io.github.mortuusars.exposure.world.item.BrokenInterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.exposure.util.Rect2f;
import net.minecraft.SharedConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

import java.util.List;

public class ViewfinderOverlay {
    public static final Identifier VIEWFINDER_TEXTURE = Exposure.resource("textures/gui/viewfinder/viewfinder.png");
    public static final Identifier NO_FILM_ICON_TEXTURE = Exposure.resource("textures/gui/viewfinder/no_film.png");
    public static final Identifier REMAINING_FRAMES_ICON_TEXTURE = Exposure.resource("textures/gui/viewfinder/remaining_frames.png");
    public static final Identifier BSOD_SAD_FACE_TEXTURE = Exposure.resource("textures/gui/viewfinder/bsod_sad_face.png");
    public static final Identifier BSOD_QR_CODE_TEXTURE = Exposure.resource("textures/gui/viewfinder/bsod_qr_code.png");

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
        this.player = Minecrft.player();
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.backgroundColor = Config.getColor(Config.Client.VIEWFINDER_BACKGROUND_COLOR);
        this.opening = new Rect2f(0, 0, 0, 0);
        recalculateOpening();

        this.scaleAnimation = new Animation(300, EasingFunction.EASE_OUT_EXPO);
        this.initialScale = 0.5f;
        this.scale = 0.5f; // Start small to animate expanding

        this.xRot = Minecrft.get().gameRenderer.getMainCamera().xRot();
        this.yRot = Minecrft.get().gameRenderer.getMainCamera().yRot();
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
        scale = Mth.lerp(scaleAnimation.getValue(), initialScale, 1f);

        // opening and scale is updated even if overlay is not rendered - other classes may depend on them.

        if (!viewfinder.isLookingThrough() || Minecrft.options().hideGui || camera.isEmpty()) return;

        final int width = Minecrft.get().getWindow().getGuiScaledWidth();
        final int height = Minecrft.get().getWindow().getGuiScaledHeight();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(width / 2f, height / 2f);
        guiGraphics.pose().scale(scale, scale);

        if (Minecrft.options().bobView().get()) {
            bobView(guiGraphics.pose(), deltaTracker);
        }
        applyAttackAnimation(guiGraphics.pose(), deltaTracker);
        applyMovementDelay(guiGraphics.pose(), deltaTracker);

        guiGraphics.pose().translate(-width / 2f, -height / 2f);

        //RenderSystem.enableBlend();
        //RenderSystem.defaultBlendFunc();
        //RenderSystem.enableDepthTest();
        //RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

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
            //RenderSystem.enableBlend();
            //RenderSystem.defaultBlendFunc();
            //RenderSystem.enableDepthTest();
            //RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        drawShutter(guiGraphics);
        drawViewfinderTexture(guiGraphics);

        // Guide
        if (drawGuide) {
            Identifier guideTexture = CameraSettings.COMPOSITION_GUIDE.getOrDefault(camera.getItemStack()).overlayTextureLocation();
            GuiUtil.blit(guiGraphics, guideTexture, opening);
        }

        if (!(Minecrft.get().screen instanceof ViewfinderCameraControlsScreen)) {
            renderStatusIcons(guiGraphics, camera.getItemStack());
        }

        guiGraphics.pose().popMatrix();
        //RenderSystem.disableDepthTest();
    }

    protected void drawViewfinderTexture(GuiGraphics guiGraphics) {
        GuiUtil.blit(guiGraphics, VIEWFINDER_TEXTURE, opening);
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
        guiGraphics.blit(BSOD_SAD_FACE_TEXTURE, x, y - sadFaceSize - margin, 0, 0, sadFaceSize, sadFaceSize, 42, 42);

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
        guiGraphics.blit(BSOD_QR_CODE_TEXTURE, x, y, 0, 0, qrCodeSize, qrCodeSize, qrCodeTextureSize, qrCodeTextureSize);

        MutableComponent errorCode = Component.translatable("item.exposure.broken_interplanar_projector.viewfinder.error_code");
        guiGraphics.drawString(font, errorCode, x + qrCodeSize + margin, y, 0xFFFFFFFF, false);
        y += font.lineHeight;
        String code = item.getErrorCode(stack);
        guiGraphics.drawString(font, code, x + qrCodeSize + margin, y, 0xFFFFFFFF, false);
    }

    public void bobView(Matrix3x2fStack poseStack, DeltaTracker deltaTracker) {
        if (Minecrft.get().getCameraEntity() instanceof AbstractClientPlayer pl) {
            Vec3 movement = pl.getDeltaMovement();
            float walkDist = (float)(movement.horizontalDistance() * 8.0);
            float strength = Mth.clamp(walkDist * 0.25f, 0.0f, 1.0f);
            float x = Mth.sin(walkDist * (float) Math.PI) * strength;
            float y = Math.abs(Mth.cos(walkDist * (float) Math.PI) * strength);

            float delta = Math.min(deltaTracker.getGameTimeDeltaTicks(), 1f);
            bobX = Mth.lerp(delta, bobX, x);
            bobY = Mth.lerp(delta, bobY, y);
            double guiScale = Minecrft.get().getWindow().getGuiScale();
            poseStack.translate((float)(bobX * 100 / guiScale), (float)(bobY * 200 / guiScale));
            float scale = this.scale - (bobY * 0.25f);
            poseStack.scale(scale, scale);
        }
    }

    public void applyAttackAnimation(Matrix3x2fStack poseStack, DeltaTracker deltaTracker) {
        float attack = player.attackAnim;
        if (attack > 0.1f)
            attack = 1f - attack;

        float delta = Math.min(deltaTracker.getGameTimeDeltaTicks(), 1f);
        attackAnim = Mth.lerp(delta, attackAnim, attack);

        poseStack.scale(1f - attackAnim * 0.1f, 1f - attackAnim * 0.2f);
        poseStack.rotate(-(float)Math.toRadians(Mth.lerp(attackAnim, 0, 5)));
        double guiScale = Minecrft.get().getWindow().getGuiScale();
        poseStack.translate(0f, (float)(60f / guiScale * attackAnim));
    }

    public void applyMovementDelay(Matrix3x2fStack poseStack, DeltaTracker deltaTracker) {
        float delta = Math.min(deltaTracker.getGameTimeDeltaTicks() * 0.6f, 1.0f);
        xRot0 = Mth.lerp(delta, xRot0, xRot);
        yRot0 = Mth.lerp(delta, yRot0, yRot);
        xRot = Minecrft.get().gameRenderer.getMainCamera().xRot();
        yRot = Minecrft.get().gameRenderer.getMainCamera().yRot();
        double guiScale = Minecrft.get().getWindow().getGuiScale();
        double horizontalDelay = (yRot - yRot0) / guiScale * 3;
        double verticalDelay = (xRot - xRot0) / guiScale * 3;
        poseStack.translate((float)-horizontalDelay, (float)-verticalDelay);
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

    protected void renderStatusIcons(GuiGraphics guiGraphics, ItemStack cameraStack) {
        ItemStack filmStack = Attachment.FILM.get(cameraStack).getForReading();

        if (filmStack.isEmpty()
                || !(filmStack.getItem() instanceof FilmRollItem filmRollItem)
                || !filmRollItem.canAddFrame(filmStack)) {
            renderNoFilmIcon(guiGraphics);
            return;
        }

        renderRemainingFramesIcon(guiGraphics, filmRollItem, filmStack);
    }

    protected void renderNoFilmIcon(GuiGraphics guiGraphics) {
        int x = (int) ((opening.x + (opening.width / 2) - 12));
        int y = (int) (opening.y + opening.height - 18);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, NO_FILM_ICON_TEXTURE, x, y, 0, 0, 23, 18, 23, 18);
    }

    protected void renderRemainingFramesIcon(GuiGraphics guiGraphics, FilmRollItem filmRollItem, ItemStack filmStack) {
        int maxFrames = filmRollItem.getMaxFrameCount(filmStack);
        int exposedFrames = filmRollItem.getStoredFramesCount(filmStack);
        int remainingFrames = Math.max(0, maxFrames - exposedFrames);
        if (maxFrames > 5 && remainingFrames <= 3) {
            int x = (int) (opening.x + (opening.width / 2) - 17);
            int y = (int) (opening.y + opening.height - 15);
            int vOffset = (remainingFrames - 1) * 15;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, REMAINING_FRAMES_ICON_TEXTURE, x, y, 0, vOffset, 33, 15, 33, 45);
        }
    }
}
