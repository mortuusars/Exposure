package io.github.mortuusars.exposure.client.render.item.range;

import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.world.item.ChromaticSheetItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record Channels() implements RangeSelectItemModelProperty {
    public static final MapCodec<Channels> MAP_CODEC = MapCodec.unit(new Channels());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner entity, int seed) {
        if (stack.getItem() instanceof ChromaticSheetItem chromaticSheet) {
            return chromaticSheet.getLayers(stack).size() / 10f;
        }
        return 0f;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}
