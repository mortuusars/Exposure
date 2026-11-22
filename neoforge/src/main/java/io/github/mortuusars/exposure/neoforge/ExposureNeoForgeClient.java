package io.github.mortuusars.exposure.neoforge;

import io.github.mortuusars.exposure.Exposure;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Exposure.ID, dist = Dist.CLIENT)
public class ExposureNeoForgeClient {
    public ExposureNeoForgeClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
