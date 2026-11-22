package io.github.mortuusars.exposure.commands.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ColorPaletteArgument extends IdentifierArgument {
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        SharedSuggestionProvider provider = (SharedSuggestionProvider) context.getSource();
        Set<Identifier> keys = provider.registryAccess().lookupOrThrow(Exposure.Registries.COLOR_PALETTE).keySet();
        return SharedSuggestionProvider.suggestResource(keys, builder);
    }
}
