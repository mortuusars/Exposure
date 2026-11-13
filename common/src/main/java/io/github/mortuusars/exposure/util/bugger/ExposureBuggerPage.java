package io.github.mortuusars.exposure.util.bugger;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.gui.screen.test.TestImageScreen;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.mortaar.bugger.screen.page.BuggerScreenPage;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.mortaar.util.color.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExposureBuggerPage implements BuggerScreenPage {
    protected boolean isCapturing;
    @Nullable
    protected RenderableImage renderableImage;

    @Override
    public String getTitle() {
        return "Exposure";
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTicks, float scale) {
        if (!isCapturing && renderableImage != null) {
            guiGraphics.pose().pushPose();
            float size = 256 * scale;
            guiGraphics.pose().translate(5, 5, 0);
            guiGraphics.pose().scale(size, size, size);
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            ExposureClient.imageRenderer().render(renderableImage, guiGraphics.pose(), bufferSource, RenderCoordinates.DEFAULT, Color.WHITE);
            bufferSource.endBatch();
            guiGraphics.pose().popPose();
        }
    }

    @Override
    public boolean onKeyPress(int key, int scanCode, int modifiers) {
        if (key == InputConstants.KEY_END) {
            Minecrft.get().setScreen(new TestImageScreen());
            return true;
        }
        if (key == InputConstants.KEY_INSERT) {
            if (isCapturing) {
                return true;
            }

            isCapturing = true;
            ExposureClient.cycles().enqueueTask(Capture.of(Capture.screenshot(),
                        CaptureAction.setCameraEntity(Minecrft.playerOrThrow()),
                        CaptureAction.optional(Optional.of(60.0), CaptureAction::setFov),
                        CaptureAction.hideGui(),
                        CaptureAction.optional(Config.Client.KEEP_POST_EFFECT.isFalse(), CaptureAction::disablePostEffect),
                        // CaptureAction.setFilter(params.filter()),
                        CaptureAction.modifyGamma(new ShutterSpeed("1/30"))
                        //CaptureAction.optional(params.getFlash(), () -> CaptureAction.flash(entity)),
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
            return true;
        }

        return BuggerScreenPage.super.onKeyPress(key, scanCode, modifiers);
    }

    private void setImage(Image image) {
        if (this.renderableImage != null) {
            this.renderableImage.close();
        }

        this.renderableImage = RenderableImage.of("test_image", image);
        ExposureClient.imageRenderer().clearCacheOf("test_image");
        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.SHUTTER_CLOSE.get(), 1f));
        isCapturing = false;
    }
}
