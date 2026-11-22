package io.github.mortuusars.exposure.neoforge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Lens;
import io.github.mortuusars.exposure.event.CommonEvents;
import io.github.mortuusars.exposure.event.ServerEvents;
import io.github.mortuusars.exposure.network.neoforge.PacketsImpl;
import io.github.mortuusars.exposure.network.packet.C2SPackets;
import io.github.mortuusars.exposure.network.packet.CommonPackets;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.S2CPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@SuppressWarnings("unused")
public class NeoForgeCommonEvents {
    @EventBusSubscriber(modid = Exposure.ID)
    public static class ModBus {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                // Makes stats show up in stat screen
                Exposure.Stats.STATS.forEach((location, statFormatter) -> {
                    Stats.CUSTOM.get(location);
                });
            });
        }

        @SubscribeEvent
        public static void addDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(Exposure.Registries.COLOR_PALETTE, ColorPalette.CODEC, ColorPalette.CODEC);
            event.dataPackRegistry(Exposure.Registries.LENS, Lens.CODEC, Lens.CODEC);
            event.dataPackRegistry(Exposure.Registries.FILTER, Filter.CODEC, Filter.CODEC);
        }

        @SuppressWarnings("unchecked")
        @SubscribeEvent
        public static void registerPackets(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            // This monstrosity is to avoid having to define packets for forge and fabric separately.
            for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : S2CPackets.getDefinitions()) {
                registrar.playToClient((CustomPacketPayload.Type<Packet>) definition.type(),
                        (StreamCodec<FriendlyByteBuf, Packet>) definition.codec());
            }

            for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : C2SPackets.getDefinitions()) {
                registrar.playToServer((CustomPacketPayload.Type<Packet>) definition.type(),
                        (StreamCodec<FriendlyByteBuf, Packet>) definition.codec(), PacketsImpl::handle);
            }

            for (CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload> definition : CommonPackets.getDefinitions()) {
                registrar.playBidirectional((CustomPacketPayload.Type<Packet>) definition.type(),
                        (StreamCodec<FriendlyByteBuf, Packet>) definition.codec(), PacketsImpl::handle);
            }
        }

//        @SubscribeEvent
//        public static void onCreativeTabsBuild(BuildCreativeModeTabContentsEvent event) {
//            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
//                event.accept(Exposure.Items.CAMERA.get());
//                event.accept(Exposure.Items.BLACK_AND_WHITE_FILM.get());
//                event.accept(Exposure.Items.COLOR_FILM.get());
//                event.accept(Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get());
//                event.accept(Exposure.Items.DEVELOPED_COLOR_FILM.get());
//                event.accept(Exposure.Items.PHOTOGRAPH.get());
//                event.accept(Exposure.Items.AGED_PHOTOGRAPH.get());
//                event.accept(Exposure.Items.INTERPLANAR_PROJECTOR.get());
//                event.accept(Exposure.Items.STACKED_PHOTOGRAPHS.get());
//                event.accept(Exposure.Items.PHOTOGRAPH_FRAME.get());
//                event.accept(Exposure.Items.CLEAR_PHOTOGRAPH_FRAME.get());
//                event.accept(Exposure.Items.CAMERA_STAND.get());
//                event.accept(Exposure.Items.ALBUM.get());
//            }
//
//            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
//                event.accept(Exposure.Items.LIGHTROOM.get());
//            }
//        }

        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(Capabilities.Item.BLOCK, Exposure.BlockEntityTypes.LIGHTROOM.get(),
                    (be, side) -> side == null ? VanillaContainerWrapper.of(be) : new WorldlyContainerWrapper(be, side));
        }
    }

    @EventBusSubscriber(modid = Exposure.ID)
    public static class GameBus {
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
