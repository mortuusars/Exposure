package io.github.mortuusars.exposure.data.generation.provider;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class LootTablesProvider extends LootTableProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final DataGenerator generator;

    public LootTablesProvider(DataGenerator generator) {
        //noinspection unchecked
        super(generator.getPackOutput(), BuiltInLootTables.all(), Collections.EMPTY_LIST);
        this.generator = generator;
    }

    @Override
    public @NotNull List<SubProviderEntry> getTables() {
        return super.getTables();
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return CompletableFuture.runAsync(() -> dropsSelf(cache, Exposure.Items.LIGHTROOM.get()));
    }

    @SuppressWarnings("unused")
    public LootPoolSingletonContainer.Builder<?> item(ItemLike item, int count) {
        return item(item, count, count);
    }

    public LootPoolSingletonContainer.Builder<?> item(ItemLike item, int min, int max) {
        LootPoolSingletonContainer.Builder<?> itemBuilder = LootItem.lootTableItem(item);

        if (min == max)
            itemBuilder.apply(SetItemCountFunction.setCount(ConstantValue.exactly(min)));
        else
            itemBuilder.apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));

        return itemBuilder;
    }

    private void dropsSelf(CachedOutput cache, BlockItem blockItem) {
        writeTable(cache, Exposure.resource("blocks/" + Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(blockItem)).getPath()),
                LootTable.lootTable()
                        .setParamSet(LootContextParamSets.BLOCK)
                        .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(blockItem)))
                        .build());
    }

    private void writeTable(CachedOutput cache, ResourceLocation location, LootTable lootTable) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        Path path = outputFolder.resolve("data/" + location.getNamespace() + "/loot_tables/" + location.getPath() + ".json");
        try {
            DataProvider.saveStable(cache, LootDataType.TABLE.parser().toJsonTree(lootTable), path);
        } catch (Exception e) {
            LOGGER.error("Couldn't write loot lootTable {}", path, e);
        }
    }
}