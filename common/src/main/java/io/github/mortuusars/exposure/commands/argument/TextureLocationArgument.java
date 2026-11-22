package io.github.mortuusars.exposure.commands.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class TextureLocationArgument extends IdentifierArgument {
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(getTextureLocations(), builder);
    }

    private static Stream<Identifier> getTextureLocations() {
        return Minecraft.getInstance().getResourceManager()
                .listResources("textures", rl -> true)
                .keySet()
                .stream();
    }
}
