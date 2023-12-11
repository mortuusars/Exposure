package io.github.mortuusars.exposure.data.generation.provider;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BlockTagsProvider extends net.minecraftforge.common.data.BlockTagsProvider {
    public BlockTagsProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), provider, Exposure.ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE)
                .add(Exposure.Blocks.LIGHTROOM.get());
    }
}