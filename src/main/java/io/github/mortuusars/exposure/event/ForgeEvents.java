package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.storage.ServersideExposureStorage;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerLifecycleEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    @SubscribeEvent
    public static void serverAboutToStart(ServerAboutToStartEvent event) {
    }
}
