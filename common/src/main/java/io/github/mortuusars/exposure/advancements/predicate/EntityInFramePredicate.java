package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.camera.frame.EntityInFrame;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record EntityInFramePredicate(Optional<Identifier> type,
                                     Optional<String> name,
                                     MinMaxBounds.Ints distance) {
    public static final Codec<EntityInFramePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("type").forGetter(EntityInFramePredicate::type),
            Codec.STRING.optionalFieldOf("name").forGetter(EntityInFramePredicate::name),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("distance", MinMaxBounds.Ints.ANY).forGetter(EntityInFramePredicate::distance)
    ).apply(instance, EntityInFramePredicate::new));

    public boolean matches(EntityInFrame entity) {
        if (type.isPresent() && !type.get().equals(entity.id())) return false;
        if (name.isPresent() && !name.get().equals(entity.name())) return false;
        return distance().matches(entity.distance());
    }

    public boolean matches(List<EntityInFrame> entitiesInFrame) {
        return entitiesInFrame.stream().anyMatch(this::matches);
    }
}
