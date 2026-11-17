package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.capture.CapturePostEffect;
import io.github.mortuusars.exposure.client.util.Shader;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class SetFilterAction implements CaptureAction {
    protected Optional<ResourceLocation> filter;

    public SetFilterAction(Optional<ResourceLocation> filter) {
        this.filter = filter;
    }

    @Override
    public void beforeCapture() {
        Shader.setSuppressViewfinder(true);
        filter.ifPresent(CapturePostEffect::apply);
    }

    @Override
    public void afterCapture() {
        Shader.setSuppressViewfinder(false);
        filter.ifPresent(f -> CapturePostEffect.remove());
    }
}
