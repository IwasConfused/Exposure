package io.github.mortuusars.exposure.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.core.ExposureIdentifier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ExposureIdSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> ids = ExposureServer.exposureRepository().getAllIdentifiers().stream()
                .map(ExposureIdentifier::toValueString)
                .toList();
        // I wanted to add ID sorting by gameTime, but suggestions are sorted alphabetically down the line anyway.
        // It can be done probably, but it's not worth the hassle
        return SharedSuggestionProvider.suggest(ids, builder);
    }
}
