package io.github.mortuusars.exposure.fabric.resources;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class IdentifiableResourceReloadListenerWrapper implements IdentifiableResourceReloadListener {
	private final Identifier fabricId;
	private final PreparableReloadListener listener;

	public IdentifiableResourceReloadListenerWrapper(Identifier id, PreparableReloadListener listener) {
		this.fabricId = id;
		this.listener = listener;
	}

	@Override
	public Identifier getFabricId() {
		return this.fabricId;
	}

	@Override
	public @NotNull CompletableFuture<Void> reload(SharedState sharedState, Executor executor,
			PreparationBarrier preparationBarrier, Executor executor2) {
		return listener.reload(sharedState, executor, preparationBarrier, executor2);
	}
}
