package io.github.mortuusars.exposure.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ColorPaletteArgument extends ResourceLocationArgument {
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        SharedSuggestionProvider provider = (SharedSuggestionProvider) context.getSource();
        Set<ResourceLocation> keys = provider.registryAccess().registryOrThrow(Exposure.Registries.COLOR_PALETTES).keySet();
        return SharedSuggestionProvider.suggestResource(keys, builder);
    }
}
