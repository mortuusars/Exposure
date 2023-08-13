package io.github.mortuusars.exposure.data.generation.provider;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class BlockTagsProvider extends net.minecraft.data.tags.BlockTagsProvider {
    public BlockTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, Exposure.ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE)
                .add(Exposure.Blocks.LIGHTROOM.get());
    }
}