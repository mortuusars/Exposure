package io.github.mortuusars.exposure.data.generation.provider;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ItemTagsProvider extends net.minecraft.data.tags.ItemTagsProvider {
    public ItemTagsProvider(DataGenerator generator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, blockTagsProvider, Exposure.ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(Exposure.Tags.Items.PHOTO_PAPERS).add(Items.PAPER);
        tag(Exposure.Tags.Items.FILM_ROLLS).add(Exposure.Items.BLACK_AND_WHITE_FILM.get(), Exposure.Items.COLOR_FILM.get());
        tag(Exposure.Tags.Items.DEVELOPED_FILMS).add(Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get(), Exposure.Items.DEVELOPED_COLOR_FILM.get());
    }

    private void optionalTags(TagAppender<Item> tag, String namespace, String... items) {
        for (String item : items) {
            tag.addOptionalTag(new ResourceLocation(namespace, item));
        }
    }

    private void optional(TagAppender<Item> tag, String namespace, String... items) {
        for (String item : items) {
            tag.addOptional(new ResourceLocation(namespace, item));
        }
    }
}