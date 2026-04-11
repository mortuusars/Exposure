package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.mixin.client.GameRendererAccessor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class SetPostEffectAction implements CaptureAction {
    @Nullable
    private Identifier currentEffect;
    private boolean effectActive;

    private final Identifier effect;

    public SetPostEffectAction(Identifier effect) {
        this.effect = effect;
    }

    @Override
    public void beforeCapture() {
        this.currentEffect = Minecrft.get().gameRenderer.currentPostEffect();
        this.effectActive = ((GameRendererAccessor) Minecrft.get().gameRenderer).exposure$isEffectActive();

        ((GameRendererAccessor) Minecrft.get().gameRenderer).exposure$setPostEffect(effect);
    }

    @Override
    public void afterCapture() {
        ((GameRendererAccessor) Minecrft.get().gameRenderer).exposure$setEffectActive(effectActive);
        if (currentEffect != null) {
            ((GameRendererAccessor) Minecrft.get().gameRenderer).exposure$setPostEffect(currentEffect);
        } else {
            Minecrft.get().gameRenderer.clearPostEffect();
        }
    }
}
