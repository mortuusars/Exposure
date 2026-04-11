package io.github.mortuusars.exposure.fabric;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Lens;
import io.github.mortuusars.exposure.event.CommonEvents;
import io.github.mortuusars.exposure.event.ServerEvents;
import io.github.mortuusars.exposure.network.fabric.FabricC2SPackets;
import io.github.mortuusars.exposure.network.fabric.FabricS2CPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ExposureFabric implements ModInitializer {
    // Server field to access when no other objects are available to get it from.
    public static @Nullable MinecraftServer server = null;

    @Override
    public void onInitialize() {
        Exposure.init();

        DynamicRegistries.registerSynced(Exposure.Registries.COLOR_PALETTE, ColorPalette.CODEC, ColorPalette.CODEC);
        DynamicRegistries.registerSynced(Exposure.Registries.LENS, Lens.CODEC, Lens.CODEC);
        DynamicRegistries.registerSynced(Exposure.Registries.FILTER, Filter.CODEC, Filter.CODEC);

        ConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.SERVER, Config.Server.SPEC);
        ConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.COMMON, Config.Common.SPEC);
        ConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.CLIENT, Config.Client.SPEC);

        CommandRegistrationCallback.EVENT.register(CommonEvents::registerCommands);

        Exposure.Stats.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerEvents.serverStarted(server);
            ExposureFabric.server = server;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ServerEvents.serverStopped(server);
            ExposureFabric.server = null;
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> ServerEvents.syncDatapack(Stream.of(player)));

        LootTableEvents.MODIFY.register(ExposureFabric::modifyLoot);

        FabricC2SPackets.register();
        FabricS2CPackets.register();
    }

    private static void modifyLoot(ResourceKey<LootTable> tableKey, LootTable.Builder builder,
                                   LootTableSource source, HolderLookup.Provider provider) {
        if (!Config.Common.GENERATE_LOOT.get() || !source.isBuiltin())
            return;

        if (BuiltInLootTables.SIMPLE_DUNGEON.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(Exposure.LootTables.SIMPLE_DUNGEON_INJECT))
                    .build());
        }
        if (BuiltInLootTables.ABANDONED_MINESHAFT.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(Exposure.LootTables.ABANDONED_MINESHAFT_INJECT))
                    .build());
        }
        if (BuiltInLootTables.STRONGHOLD_CROSSING.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(Exposure.LootTables.STRONGHOLD_CROSSING_INJECT))
                    .build());
        }
        if (BuiltInLootTables.VILLAGE_PLAINS_HOUSE.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(Exposure.LootTables.VILLAGE_PLAINS_HOUSE_INJECT))
                    .build());
        }
        if (BuiltInLootTables.SHIPWRECK_MAP.equals(tableKey)) {
            builder.pool(LootPool.lootPool()
                    .add(NestedLootTable.lootTableReference(Exposure.LootTables.SHIPWRECK_MAP_INJECT))
                    .build());
        }
    }
}
