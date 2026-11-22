package io.github.mortuusars.exposure.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.advancements.predicate.FramePredicate;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FramePrintedTrigger extends SimpleCriterionTrigger<FramePrintedTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player,
                        BlockPos pos,
                        Frame frame,
                        ItemStack result) {
        this.trigger(player, triggerInstance ->
                triggerInstance.matches(player, pos, frame, result));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<LocationPredicate> location,
                                  Optional<FramePredicate> frame,
                                  Optional<ItemPredicate> item) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location),
                        FramePredicate.CODEC.optionalFieldOf("frame").forGetter(TriggerInstance::frame),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item))
                .apply(instance, TriggerInstance::new));

        public boolean matches(ServerPlayer player,
                               BlockPos pos,
                               Frame frame,
                               ItemStack result) {
            return (location.isEmpty() || location.get().matches(player.level(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5))
                    && (this.frame.isEmpty() || this.frame.get().matches(frame))
                    && (item.isEmpty() || item.get().test(result));
        }
    }
}