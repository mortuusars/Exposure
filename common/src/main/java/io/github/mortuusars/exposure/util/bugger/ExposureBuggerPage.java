package io.github.mortuusars.exposure.util.bugger;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.gui.screen.test.TestImageScreen;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.camera.capture.DitherMode;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.mortaar.bugger.screen.page.BuggerScreenPage;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.mortaar.util.color.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ExposureBuggerPage implements BuggerScreenPage {
    protected boolean isCapturing;
    @Nullable
    protected RenderableImage renderableImage;

    protected List<RenderableImage> videoFrames = new ArrayList<>();
    protected long lastVideoFrameTime;
    protected boolean recordingVideo;
    protected int maxFrames;

    @Override
    public String getTitle() {
        return "Exposure";
    }

    @Override
    public List<String> getLeftLines() {
        List<String> lines = new ArrayList<>();

        lines.add("Rendered Exposures");
        ExposureClient.renderedExposures().addDebugInfo(line -> lines.add("  " + line));

        lines.add("Image Renderer");
        ExposureClient.imageRenderer().addDebugInfo(line -> lines.add("  " + line));

        return lines;
    }

    // --

    @Override
    public void render(GuiGraphics graphics, float partialTicks, float scale) {
        if (!isCapturing) {
            // Image
            if (renderableImage != null) {
                graphics.pose().pushPose();
                graphics.pose().scale(scale, scale, scale);

                graphics.pose().pushPose();
                int size = 256;
                float right = Minecrft.get().getWindow().getGuiScaledWidth() / scale;
                float bottom = Minecrft.get().getWindow().getGuiScaledHeight() / scale;
                graphics.pose().translate(3, bottom - size - 3, 0);
                graphics.fill(-2, -2, size + 2, size + 2, 0xFF333333);
                graphics.pose().scale(size, size, size);
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                ExposureClient.imageRenderer().render(renderableImage, graphics.pose(), bufferSource, RenderCoordinates.DEFAULT, Color.WHITE);
                bufferSource.endBatch();
                graphics.pose().popPose();

                graphics.pose().popPose();
            }

            // Video
            if (Util.getMillis() - lastVideoFrameTime >= 500 && !videoFrames.isEmpty()) {
                try {
                    int frames = videoFrames.size();
                    int fps = 20;
                    int mspf = 1000 / fps;
                    int videoLengthMs = frames * mspf;
                    int currentFrame = Mth.clamp((int) (Util.getMillis() % videoLengthMs / mspf), 0, frames - 1);

                    RenderableImage frame = videoFrames.get(currentFrame);

                    graphics.pose().pushPose();
                    float size = 256 * scale;
                    float screenWidth = Minecrft.get().getWindow().getWidth();
                    graphics.pose().translate(2, 2, 0);
                    graphics.pose().scale(size, size, size);
                    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                    ExposureClient.imageRenderer().render(frame, graphics.pose(), bufferSource, RenderCoordinates.DEFAULT, Color.WHITE);
                    bufferSource.endBatch();
                    graphics.pose().popPose();

                    graphics.pose().pushPose();
                    graphics.pose().translate(5, size + 5, 0);
                    graphics.fill(0, 0, (int) (256 * scale), 4, 0xFF333333);
                    float percentage = (float) currentFrame / frames;
                    graphics.fill(1, 1, 1 + (int) (254 * scale * percentage), 3, 0xFFF64124);
                    graphics.pose().popPose();

                } catch (Exception e) {
                    graphics.drawString(Minecrft.get().font, e.getMessage(), 5, 5, 0xFFFF5555, false);
                }
            }
        }

        if (recordingVideo) {
            graphics.fill(5, 5, 20, 20, 0xFFFF4444);
            graphics.drawString(Minecrft.get().font, videoFrames.size() + "/" + maxFrames, 5, 27, 0xFFFFFFFF, false);
            tryCaptureVideoFrame();
        }
    }

    @Override
    public boolean onKeyPress(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_END) {
            Minecrft.get().setScreen(new TestImageScreen());
            return true;
        }

        if (handleImageCaptureKeys(key)) return true;
        if (handleVideoCaptureKeys(key)) return true;
        return BuggerScreenPage.super.onKeyPress(key, scanCode, modifiers);
    }

    // -- Image

    protected boolean handleImageCaptureKeys(int key) {
        if (key == InputConstants.KEY_INSERT) {
            if (isCapturing) {
                return true;
            }

            if (Screen.hasShiftDown()) {
                setImage(null); // Clear
            } else {
                isCapturing = true;
                ExposureClient.cycles().enqueueTask(Capture.of(Capture.screenshot(),
                            CaptureAction.setCameraEntity(Minecrft.playerOrThrow()),
                            CaptureAction.optional(Optional.of(60.0), CaptureAction::setFov),
                            CaptureAction.hideGui(),
                            CaptureAction.optional(Config.Client.KEEP_POST_EFFECT.isFalse(), CaptureAction::disablePostEffect),
                            CaptureAction.modifyGamma(new ShutterSpeed("1/30"))
                      )
                      .handleErrorAndGetResult(err -> Minecrft.execute(() -> {
                          Minecrft.playerOrThrow().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false);
                          isCapturing = false;
                      }))
                      .thenAsync(ImageEffect.Crop.SQUARE_CENTER::modify)
                      .onError(err -> Minecrft.execute(() -> {
                          Minecrft.playerOrThrow().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false);
                          isCapturing = false;
                      }))
                      .accept(this::setImage));
            }
            return true;
        }
        return false;
    }


    protected void setImage(@Nullable Image image) {
        if (this.renderableImage != null) {
            this.renderableImage.close();
        }

        this.renderableImage = image == null ? null : RenderableImage.of("test_image", image);
        ExposureClient.imageRenderer().clearCacheOf("test_image");
        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.SHUTTER_CLOSE.get(), 1f));
        isCapturing = false;
    }

    // -- Video

    protected boolean handleVideoCaptureKeys(int key) {
        if (key == InputConstants.KEY_DELETE) {
            if (Screen.hasShiftDown()) {
                videoFrames.forEach(f -> {
                    f.close();
                    ExposureClient.imageRenderer().clearCacheOf(f.getIdentifier().base());
                });
                videoFrames.clear();
            } else {
                recordingVideo = !recordingVideo;
            }
            return true;
        }
        return false;
    }

    protected boolean tryCaptureVideoFrame() {
        int fps = 20;
        int msPerFrame = 1000 / fps;

        if (isCapturing || Util.getMillis() - lastVideoFrameTime < msPerFrame) {
            return true;
        }

        isCapturing = true;
        lastVideoFrameTime = Util.getMillis();

        ExposureClient.cycles().enqueueTask(Capture.of(Capture.screenshot(),
                    CaptureAction.setCameraEntity(Minecrft.playerOrThrow()),
                    CaptureAction.optional(Optional.of(60.0), CaptureAction::setFov),
                    CaptureAction.hideGui(),
                    CaptureAction.optional(Config.Client.KEEP_POST_EFFECT.isFalse(), CaptureAction::disablePostEffect)
                    // CaptureAction.modifyGamma(new ShutterSpeed("1/30"))
              )
              .handleErrorAndGetResult(err -> Minecrft.execute(() -> {
                  Minecrft.playerOrThrow().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false);
                  isCapturing = false;
              }))
              .thenAsync(applyVideoEffects())
              .thenAsync(Palettizer.fromDitherMode(DitherMode.DITHERED)
                    .palettizeAndClose(ColorPalettes.get(Minecrft.registryAccess(), ColorPalettes.DEFAULT).value()))
              .onError(err -> Minecrft.execute(() -> {
                  Minecrft.playerOrThrow().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false);
                  isCapturing = false;
              }))
              .accept(this::addVideoFrame));
        return false;
    }

    protected Function<Image, Image> applyVideoEffects() {
        return ImageEffect.chain(
              ImageEffect.Crop.SQUARE_CENTER,
//              ImageEffect.Crop.factor(params.cropFactor()),
              ImageEffect.Resize.to(64)
//              ImageEffect.contrast(style.contrast()),
//              ImageEffect.levels(style.levels()),
//              ImageEffect.hsb(style.hsb()),
//              ImageEffect.noise(style.noise()),
//              ImageEffect.optional(params.filmProperties().type() == ExposureType.BLACK_AND_WHITE,
//                    params.singleChannel()
//                          .map(ImageEffect::singleChannelBlackAndWhite)
//                          .orElse(ImageEffect.BLACK_AND_WHITE))
        );
    }

    protected void addVideoFrame(Image image) {
        maxFrames = 128;

        if (videoFrames.size() >= maxFrames) {
            RenderableImage firstFrame = videoFrames.removeFirst();
            firstFrame.close();
            ExposureClient.imageRenderer().clearCacheOf(firstFrame.getIdentifier().base());
        }

        videoFrames.add(RenderableImage.of("video_" + Minecrft.levelOrThrow().getGameTime(), image));
        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.SHUTTER_CLOSE.get(), 1f, 0.2f));
        isCapturing = false;
    }
}
