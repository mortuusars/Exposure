package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.storage.ClientsideExposureStorage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {

    }
}
