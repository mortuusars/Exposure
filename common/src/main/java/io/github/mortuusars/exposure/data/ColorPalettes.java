package io.github.mortuusars.exposure.data;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public class ColorPalettes {
    public static final ResourceKey<ColorPalette> DEFAULT = createKey(Exposure.resource("map_colors_plus"));
    public static final ResourceKey<ColorPalette> MAP_COLORS = createKey(Exposure.resource("map_colors"));

    public static ResourceKey<ColorPalette> createKey(Identifier location) {
        return ResourceKey.create(Exposure.Registries.COLOR_PALETTE, location);
    }

    public static Holder<ColorPalette> get(RegistryAccess registryAccess, ResourceKey<ColorPalette> key) {
        Registry<ColorPalette> registry = registryAccess.lookupOrThrow(Exposure.Registries.COLOR_PALETTE);
        return registry.get(key).or(() -> registry.get(DEFAULT)).or(registry::getAny).orElseThrow();
    }

    public static Holder<ColorPalette> get(RegistryAccess registryAccess, Identifier key) {
        Registry<ColorPalette> registry = registryAccess.lookupOrThrow(Exposure.Registries.COLOR_PALETTE);
        return registry.get(key).or(() -> registry.get(DEFAULT)).or(registry::getAny).orElseThrow();
    }

    public static Holder<ColorPalette> getDefault(RegistryAccess registryAccess) {
        return get(registryAccess, DEFAULT);
    }
}
