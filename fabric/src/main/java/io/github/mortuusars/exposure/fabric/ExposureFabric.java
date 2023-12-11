package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Exposure;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.intellij.lang.annotations.Identifier;

public class ExposureFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Exposure.init();
    }
}
