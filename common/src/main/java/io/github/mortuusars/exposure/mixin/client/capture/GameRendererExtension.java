package io.github.mortuusars.exposure.mixin.client.capture;

import io.github.mortuusars.exposure.client.ExposureGameRenderer;
import io.github.mortuusars.exposure.client.ExposureStore;
import io.github.mortuusars.exposure.client.RenderedExposures;
import io.github.mortuusars.exposure.client.render.image.ImageRenderer;
import io.github.mortuusars.exposure.client.render.photograph.PhotographRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererExtension implements ExposureGameRenderer {
    @Unique
    private final ExposureStore exposure$exposureStore = new ExposureStore();
    @Unique
    private final RenderedExposures exposure$renderedExposures = new RenderedExposures();
    @Unique
    private final ImageRenderer exposure$imageRenderer = new ImageRenderer();
    @Unique
    private final PhotographRenderer exposure$photographRenderer = new PhotographRenderer();

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public ExposureStore getExposureStore() {
        return exposure$exposureStore;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public RenderedExposures getRenderedExposures() {
        return exposure$renderedExposures;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public ImageRenderer getExposureImageRenderer() {
        return exposure$imageRenderer;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public PhotographRenderer getExposurePhotographRenderer() {
        return exposure$photographRenderer;
    }

    // --

    @Inject(method = "close", at = @At("RETURN"))
    private void onClose(CallbackInfo ci) {
        exposure$exposureStore.clear();
        exposure$renderedExposures.close();
        exposure$imageRenderer.close();
    }

    @Inject(method = "resetData", at = @At("RETURN"))
    private void onResetData(CallbackInfo ci) {
        exposure$exposureStore.clear();
        exposure$renderedExposures.clearCache();
        exposure$imageRenderer.resetData();
    }
}
