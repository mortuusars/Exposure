package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.Packets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.EffectiveSide;


@Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Packets::register);

        Dist dist = FMLEnvironment.dist;
        LogicalSide logicalSide = EffectiveSide.get();
        boolean a = true;
    }
}