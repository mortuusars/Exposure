package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.neoforge.PlatformHelperClientImpl;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;

public class PlatformHelperClient {
    public static BlockStateModel getModel(Identifier model) {
        return PlatformHelperClientImpl.getModel(model);
    }
}
