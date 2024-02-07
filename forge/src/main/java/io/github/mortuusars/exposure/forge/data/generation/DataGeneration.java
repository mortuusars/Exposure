package io.github.mortuusars.exposure.forge.data.generation;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.forge.data.generation.provider.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new AdvancementsProvider(generator, provider, helper));
        generator.addProvider(event.includeServer(), new LootTablesProvider(generator));
        generator.addProvider(event.includeServer(), new RecipesProvider(generator));
        BlockTagsProvider blockTags = new BlockTagsProvider(generator, provider, helper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ItemTagsProvider(generator, provider, blockTags, helper));

        generator.addProvider(event.includeClient(), new SoundsProvider(generator, helper));
    }
}