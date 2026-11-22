package io.github.mortuusars.exposure.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.Identifier;

public record Filter(ItemPredicate predicate,
                     Identifier shader,
                     Identifier attachmentTexture,
                     Color attachmentTintColor) {
    public static final Identifier DEFAULT_GLASS_TEXTURE = Exposure.resource("textures/gui/filter/stained_glass.png");

    public static final Codec<Holder<Filter>> HOLDER_CODEC = RegistryFixedCodec.create(Exposure.Registries.FILTER);

    public static final Codec<Filter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemPredicate.CODEC.fieldOf("predicate").forGetter(Filter::predicate),
            Identifier.CODEC.fieldOf("shader").forGetter(Filter::shader),
            Identifier.CODEC.optionalFieldOf("attachment_texture", DEFAULT_GLASS_TEXTURE).forGetter(Filter::attachmentTexture),
            Color.HEX_STRING_CODEC.optionalFieldOf("attachment_tint", Color.WHITE).forGetter(Filter::attachmentTintColor)
    ).apply(instance, Filter::new));
}
