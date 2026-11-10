package io.github.mortuusars.exposure.neoforge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Lens;
import io.github.mortuusars.exposure.event.CommonEvents;
import io.github.mortuusars.exposure.event.ServerEvents;
import net.minecraft.stats.Stats;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@SuppressWarnings("unused")
public class NeoForgeCommonEvents {
    @EventBusSubscriber(modid = Exposure.ID)
    public static class ModBus {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                // Makes stats show up in stat screen
                Exposure.Stats.STATS.forEach((location, statFormatter) -> Stats.CUSTOM.get(location));
            });
        }

        @SubscribeEvent
        public static void addDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(Exposure.Registries.COLOR_PALETTE, ColorPalette.CODEC, ColorPalette.CODEC);
            event.dataPackRegistry(Exposure.Registries.LENS, Lens.CODEC, Lens.CODEC);
            event.dataPackRegistry(Exposure.Registries.FILTER, Filter.CODEC, Filter.CODEC);
        }

        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Exposure.BlockEntityTypes.LIGHTROOM.get(),
                    (be, side) -> side == null ? new InvWrapper(be) : new SidedInvWrapper(be, side));
        }

        @SubscribeEvent
        public static void serverStarted(ServerStartedEvent event) {
            ServerEvents.serverStarted(event.getServer());
        }

        @SubscribeEvent
        public static void onDatapackSync(OnDatapackSyncEvent event) {
            ServerEvents.syncDatapack(event.getRelevantPlayers());
        }

        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            CommonEvents.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        }
    }
}
