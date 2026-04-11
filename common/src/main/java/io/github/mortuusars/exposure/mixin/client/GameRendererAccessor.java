package io.github.mortuusars.exposure.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Invoker("setPostEffect")
    void exposure$setPostEffect(Identifier effect);

    @Accessor("effectActive")
    boolean exposure$isEffectActive();

    @Accessor("effectActive")
    void exposure$setEffectActive(boolean active);
}
