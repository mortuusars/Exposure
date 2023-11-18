package io.github.mortuusars.exposure.data.generation.provider;

import com.google.common.collect.Sets;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.advancement.BooleanPredicate;
import io.github.mortuusars.exposure.advancement.predicate.EntityInFramePredicate;
import io.github.mortuusars.exposure.advancement.predicate.ExposurePredicate;
import io.github.mortuusars.exposure.advancement.trigger.CameraFilmFrameExposedTrigger;
import io.github.mortuusars.exposure.camera.infrastructure.FrameData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancementsProvider extends net.minecraft.data.advancements.AdvancementProvider
{
    private final Path PATH;
    public static final Logger LOGGER = LogManager.getLogger();

    public AdvancementsProvider(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, existingFileHelper);
        PATH = dataGenerator.getOutputFolder();
    }

    @Override
    public void run(CachedOutput cache) {
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (advancement) -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
                Path path1 = getPath(PATH, advancement);

                try {
                    DataProvider.saveStable(cache, advancement.deconstruct().serializeToJson(), path1);
                }
                catch (IOException ioexception) {
                    LOGGER.error("Couldn't save advancement {}", path1, ioexception);
                }
            }
        };

        new MonobankAdvancements(this.fileHelper).accept(consumer);
    }

    private static Path getPath(Path pathIn, Advancement advancementIn) {
        return pathIn.resolve("data/" + advancementIn.getId().getNamespace() + "/advancements/" + advancementIn.getId().getPath() + ".json");
    }

    public static class MonobankAdvancements implements Consumer<Consumer<Advancement>>
    {
        private final ExistingFileHelper existingFileHelper;

        public MonobankAdvancements(ExistingFileHelper existingFileHelper) {
            this.existingFileHelper = existingFileHelper;
        }

        @Override
        public void accept(Consumer<Advancement> advancementConsumer) {

            Advancement exposure = Advancement.Builder.advancement()
                    .parent(new ResourceLocation("minecraft:adventure/root"))
                    .display(new ItemStack(Exposure.Items.CAMERA.get()),
                            Component.translatable("advancement.exposure.exposure.title"),
                            Component.translatable("advancement.exposure.exposure.description"),
                            null, FrameType.TASK, true, true, false)
                    .addCriterion("expose_film", new CameraFilmFrameExposedTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
                            LocationPredicate.ANY, ExposurePredicate.ANY))
                    .save(advancementConsumer, Exposure.resource("adventure/expose_film"), existingFileHelper);

            Advancement momentInTime = Advancement.Builder.advancement()
                    .parent(exposure)
                    .display(new ItemStack(Exposure.Items.PHOTOGRAPH.get()),
                            Component.translatable("advancement.exposure.get_photograph.title"),
                            Component.translatable("advancement.exposure.get_photograph.description"),
                            null, FrameType.TASK, true, true, false)
                    .addCriterion("get_photograph", InventoryChangeTrigger.TriggerInstance.hasItems(Exposure.Items.PHOTOGRAPH.get()))
                    .addCriterion("get_stacked_photographs", InventoryChangeTrigger.TriggerInstance.hasItems(Exposure.Items.STACKED_PHOTOGRAPHS.get()))
                    .requirements(RequirementsStrategy.OR)
                    .save(advancementConsumer, Exposure.resource("adventure/get_photograph"), existingFileHelper);

            CompoundTag flashTag = new CompoundTag();
            flashTag.putBoolean(FrameData.FLASH, true);

            Advancement flash = Advancement.Builder.advancement()
                    .parent(exposure)
                    .display(new ItemStack(Items.REDSTONE_LAMP),
                            Component.translatable("advancement.exposure.flash.title"),
                            Component.translatable("advancement.exposure.flash.description"),
                            null, FrameType.TASK, true, true, false)
                    .addCriterion("flash_in_darkness", new CameraFilmFrameExposedTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
                            LocationPredicate.ANY,
                            new ExposurePredicate(BooleanPredicate.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY,
                                    new NbtPredicate(flashTag), MinMaxBounds.Ints.atMost(4), MinMaxBounds.Ints.ANY, EntityInFramePredicate.ANY)))
                    .save(advancementConsumer, Exposure.resource("adventure/flash"), existingFileHelper);

            Advancement thatVoid = Advancement.Builder.advancement()
                    .parent(flash)
                    .display(new ItemStack(Items.END_STONE_BRICKS),
                            Component.translatable("advancement.exposure.void.title"),
                            Component.translatable("advancement.exposure.void.description"),
                            null, FrameType.TASK, true, true, true)
                    .addCriterion("photograph_in_end", new CameraFilmFrameExposedTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
                            LocationPredicate.inDimension(Level.END), ExposurePredicate.ANY))
                    .save(advancementConsumer, Exposure.resource("adventure/void"), existingFileHelper);
        }
    }
}
