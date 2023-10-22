package io.github.mortuusars.exposure.data.generation;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.generation.provider.*;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new AdvancementsProvider(generator, helper));
        generator.addProvider(event.includeServer(), new LootTablesProvider(generator));
        generator.addProvider(event.includeServer(), new RecipesProvider(generator));
        BlockTagsProvider blockTags = new BlockTagsProvider(generator, helper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ItemTagsProvider(generator, blockTags, helper));

//        BlockStatesAndModels blockStates = new BlockStatesAndModels(generator, helper);
//        generator.addProvider(event.includeClient(), blockStates);
//        generator.addProvider(event.includeClient(), new ItemModels(generator, blockStates.models().existingFileHelper));
        generator.addProvider(event.includeClient(), new SoundsProvider(generator, helper));
    }
}