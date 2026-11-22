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

public class ShaderLocationArgument extends IdentifierArgument {
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(getShaderLocations(), builder);
    }

    private static Stream<Identifier> getShaderLocations() {
        return Minecraft.getInstance().getResourceManager()
                .listResources("post_effect", ShaderLocationArgument::filterLocations)
                .keySet()
                .stream()
                .map(Identifier -> Identifier.withPath(path -> path.substring("post_chain//".length(), path.indexOf(".json"))));
    }

    private static boolean filterLocations(Identifier Identifier) {
        return Identifier.getPath().endsWith(".json")
                && !Identifier.getPath().contains("shaders/program")
                && !Identifier.getPath().contains("shaders/core");
    }
}
