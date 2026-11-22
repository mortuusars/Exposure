package io.github.mortuusars.exposure.client.render.photograph;

import org.jetbrains.annotations.Nullable;

public interface HasPhotographRenderState {
    default @Nullable PhotographRenderState getPhotographRenderState() {
        throw new IllegalStateException("This method must be implemented.");
    }

    default void setPhotographRenderState(PhotographRenderState renderState) {
        throw new IllegalStateException("This method must be implemented.");
    }
}
