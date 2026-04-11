package io.github.mortuusars.exposure.client.render.item.range;

import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record AlbumPhotos() implements RangeSelectItemModelProperty {
    public static final MapCodec<AlbumPhotos> MAP_CODEC = MapCodec.unit(new AlbumPhotos());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner entity, int seed) {
        if (stack.getItem() instanceof AlbumItem albumItem) {
            return albumItem.getPhotographsCount(stack) / 100f;
        }
        return 0f;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}
