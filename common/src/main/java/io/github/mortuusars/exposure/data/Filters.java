package io.github.mortuusars.exposure.data;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class Filters {
    public static Optional<Filter> of(RegistryAccess registryAccess, ItemStack stack) {
        return registryAccess.lookupOrThrow(Exposure.Registries.FILTER)
                .stream()
                .filter(filter -> filter.predicate().test(stack))
                .findFirst();
    }

    public static Optional<Identifier> locationOf(RegistryAccess registryAccess, Filter filter) {
        return Optional.ofNullable(registryAccess.lookupOrThrow(Exposure.Registries.FILTER).getKey(filter));
    }
}