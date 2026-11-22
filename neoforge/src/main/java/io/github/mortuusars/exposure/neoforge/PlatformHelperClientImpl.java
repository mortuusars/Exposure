package io.github.mortuusars.exposure.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.HashMap;
import java.util.Map;

public class PlatformHelperClientImpl {
    private static final Map<Identifier, StandaloneModelKey<BlockStateModel>> MODELS = new HashMap<>();

    public static void registerModel(ModelEvent.RegisterStandalone event, Identifier id) {
        StandaloneModelKey<BlockStateModel> key = MODELS.computeIfAbsent(id,
                model -> new StandaloneModelKey<>(model::toString));
        event.register(key, SimpleUnbakedStandaloneModel.blockStateModel(id));
    }

    public static BlockStateModel getModel(Identifier id) {
        return Minecraft.getInstance().getModelManager().getStandaloneModel(MODELS.get(id));
    }
}
