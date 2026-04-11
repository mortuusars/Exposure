package io.github.mortuusars.exposure.client.render.item.range;

import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record Count() implements RangeSelectItemModelProperty {
    public static final MapCodec<Count> MAP_CODEC = MapCodec.unit(new Count());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner entity, int seed) {
        if (stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem) {
            return stackedPhotographsItem.getPhotographs(stack).size() / 100f;
        }
        return 0f;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}
