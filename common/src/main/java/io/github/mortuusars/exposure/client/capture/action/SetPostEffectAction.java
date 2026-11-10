package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SetPostEffectAction implements CaptureAction {
    @Nullable
    private ResourceLocation currentEffect;
    private boolean effectActive;

    private final ResourceLocation effect;

    public SetPostEffectAction(ResourceLocation effect) {
        this.effect = effect;
    }

    @Override
    public void beforeCapture() {
        PostChain currentEffect = Minecrft.get().gameRenderer.currentEffect();
        if (currentEffect != null) {
            this.currentEffect = ResourceLocation.parse(currentEffect.getName());
        }
        this.effectActive = Minecrft.get().gameRenderer.effectActive;

        Minecrft.get().gameRenderer.loadEffect(effect);
        Minecrft.get().gameRenderer.effectActive = true;
    }

    @Override
    public void afterCapture() {
        Minecrft.get().gameRenderer.effectActive = effectActive;
        if (currentEffect != null) {
            Minecrft.get().gameRenderer.loadEffect(currentEffect);
        } else {
            Minecrft.get().gameRenderer.shutdownEffect();
        }
    }
}
