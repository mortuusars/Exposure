package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.render.photograph.HasPhotographRenderState;
import io.github.mortuusars.exposure.client.render.photograph.PhotographRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemFrameRenderState.class)
public class ItemFrameRenderStateMixin implements HasPhotographRenderState {
    @Unique
    @Nullable
    PhotographRenderState exposure$PhotographRenderState = null;

    @Override
    public @Nullable PhotographRenderState getPhotographRenderState() {
        return exposure$PhotographRenderState;
    }

    @Override
    public void setPhotographRenderState(PhotographRenderState renderState) {
        exposure$PhotographRenderState = renderState;
    }
}
