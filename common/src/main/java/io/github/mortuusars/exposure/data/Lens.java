package io.github.mortuusars.exposure.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import net.minecraft.advancements.criterion.ItemPredicate;

public record Lens(ItemPredicate predicate, FocalRange focalRange) {
    public static final Codec<Lens> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemPredicate.CODEC.fieldOf("predicate").forGetter(Lens::predicate),
            FocalRange.CODEC.fieldOf("focal_range").forGetter(Lens::focalRange)
    ).apply(instance, Lens::new));
}
