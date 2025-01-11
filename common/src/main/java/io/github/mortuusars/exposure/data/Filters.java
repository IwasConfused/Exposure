package io.github.mortuusars.exposure.data;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class Filters {
    public static Optional<Filter> of(RegistryAccess registryAccess, ItemStack stack) {
        if (!stack.is(Exposure.Tags.Items.FILTERS)) return Optional.empty();

        return registryAccess.registryOrThrow(Exposure.Registries.FILTER)
                .stream()
                .filter(filter -> filter.predicate().test(stack))
                .findFirst();
    }
}