package io.github.mortuusars.exposure.data.generation.provider;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.advancement.BooleanPredicate;
import io.github.mortuusars.exposure.advancement.predicate.EntityInFramePredicate;
import io.github.mortuusars.exposure.advancement.predicate.ExposurePredicate;
import io.github.mortuusars.exposure.advancement.trigger.CameraFilmFrameExposedTrigger;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementsProvider extends ForgeAdvancementProvider {
    public static final Logger LOGGER = LogManager.getLogger();

    public AdvancementsProvider(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), provider, existingFileHelper, List.of(new ExposureAdvancements()));
    }

    public static class ExposureAdvancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.@NotNull Provider registries, @NotNull Consumer<Advancement> consumer,
                             @NotNull ExistingFileHelper existingFileHelper) {
            Advancement exposure = Advancement.Builder.advancement()
                    .parent(new ResourceLocation("minecraft:adventure/root"))
                    .display(new ItemStack(Exposure.Items.CAMERA.get()),
                            Component.translatable("advancement.exposure.exposure.title"),
                            Component.translatable("advancement.exposure.exposure.description"),
                            null, FrameType.TASK, true, true, false)
                    .addCriterion("expose_film", new CameraFilmFrameExposedTrigger.TriggerInstance(ContextAwarePredicate.ANY,
                            LocationPredicate.ANY, ExposurePredicate.ANY))
                    .save(consumer, Exposure.resource("adventure/expose_film"), existingFileHelper);

            Advancement momentInTime = Advancement.Builder.advancement()
                    .parent(exposure)
                    .display(new ItemStack(Exposure.Items.PHOTOGRAPH.get()),
                            Component.translatable("advancement.exposure.get_photograph.title"),
                            Component.translatable("advancement.exposure.get_photograph.description"),
                            null, FrameType.TASK, true, true, false)
                    .addCriterion("get_photograph", InventoryChangeTrigger.TriggerInstance.hasItems(Exposure.Items.PHOTOGRAPH.get()))
                    .addCriterion("get_stacked_photographs", InventoryChangeTrigger.TriggerInstance.hasItems(Exposure.Items.STACKED_PHOTOGRAPHS.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(consumer, Exposure.resource("adventure/get_photograph"), existingFileHelper);

            CompoundTag flashTag = new CompoundTag();
            flashTag.putBoolean(FrameData.FLASH, true);

            Advancement flash = Advancement.Builder.advancement()
                    .parent(exposure)
                    .display(new ItemStack(Items.REDSTONE_LAMP),
                            Component.translatable("advancement.exposure.flash.title"),
                            Component.translatable("advancement.exposure.flash.description"),
                            null, FrameType.TASK, true, true, false)
                    .addCriterion("flash_in_darkness", new CameraFilmFrameExposedTrigger.TriggerInstance(ContextAwarePredicate.ANY,
                            LocationPredicate.ANY,
                            new ExposurePredicate(BooleanPredicate.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY,
                                    new NbtPredicate(flashTag), MinMaxBounds.Ints.atMost(4), MinMaxBounds.Ints.ANY, EntityInFramePredicate.ANY)))
                    .save(consumer, Exposure.resource("adventure/flash"), existingFileHelper);

            Advancement thatVoid = Advancement.Builder.advancement()
                    .parent(flash)
                    .display(new ItemStack(Items.END_STONE_BRICKS),
                            Component.translatable("advancement.exposure.void.title"),
                            Component.translatable("advancement.exposure.void.description"),
                            null, FrameType.TASK, true, true, true)
                    .addCriterion("photograph_in_end", new CameraFilmFrameExposedTrigger.TriggerInstance(ContextAwarePredicate.ANY,
                            LocationPredicate.inDimension(Level.END), ExposurePredicate.ANY))
                    .save(consumer, Exposure.resource("adventure/void"), existingFileHelper);

        }
    }
}