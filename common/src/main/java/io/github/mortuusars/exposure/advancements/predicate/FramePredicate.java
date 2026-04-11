package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record FramePredicate(Optional<ExposureIdentifier> identifier,
                             Optional<String> type,
                             Optional<String> photographer,
                             Optional<ShutterSpeedPredicate> shutterSpeed,
                             Optional<MinMaxBounds.Ints> focalLength,
                             Optional<MinMaxBounds.Ints> lightLevel,
                             Optional<MinMaxBounds.Ints> dayTime,
                             Optional<MinMaxBounds.Ints> entitiesInFrameCount,
                             Optional<List<EntityInFramePredicate>> entitiesInFrame,
                             Optional<ExtraDataPredicate> extraData) implements SingleComponentItemPredicate<Frame> {
    public static final Codec<FramePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExposureIdentifier.CODEC.optionalFieldOf("identifier").forGetter(FramePredicate::identifier),
            Codec.STRING.optionalFieldOf("type").forGetter(FramePredicate::type),
            Codec.STRING.optionalFieldOf("photographer").forGetter(FramePredicate::photographer),
            ShutterSpeedPredicate.CODEC.optionalFieldOf("shutter_speed").forGetter(FramePredicate::shutterSpeed),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("focal_length").forGetter(FramePredicate::focalLength),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("light_level").forGetter(FramePredicate::lightLevel),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("day_time").forGetter(FramePredicate::dayTime),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("entities_in_frame_count").forGetter(FramePredicate::entitiesInFrameCount),
            EntityInFramePredicate.CODEC.listOf().optionalFieldOf("entities_in_frame").forGetter(FramePredicate::entitiesInFrame),
            ExtraDataPredicate.CODEC.optionalFieldOf("extra_data").forGetter(FramePredicate::extraData)
    ).apply(instance, FramePredicate::new));

    @Override
    public @NotNull DataComponentType<Frame> componentType() {
        return Exposure.DataComponents.PHOTOGRAPH_FRAME;
    }

    public boolean matches(Frame frame) {
        return (identifier.isEmpty() || identifier.get().equals(frame.identifier()))
                && (type.isEmpty() || type.get().equals(frame.type().getSerializedName()))
                && (photographer.isEmpty() || photographer.get().equals(frame.photographer().name()))
                && (shutterSpeed.isEmpty() || shutterSpeed.get().matches(frame.extraData().get(Frame.SHUTTER_SPEED)))
                && (focalLength.isEmpty() || frame.extraData().get(Frame.FOCAL_LENGTH).map(focalLength.get()::matches).orElse(false))
                && (lightLevel.isEmpty() || frame.extraData().get(Frame.LIGHT_LEVEL).map(lightLevel.get()::matches).orElse(false))
                && (dayTime.isEmpty() || frame.extraData().get(Frame.DAY_TIME).map(dayTime.get()::matches).orElse(false))
                && (entitiesInFrameCount.isEmpty() || entitiesInFrameCount.get().matches(frame.entitiesInFrame().size()))
                && (entitiesInFrame.isEmpty() || entitiesInFrame.get().stream().allMatch(predicate -> predicate.matches(frame.entitiesInFrame())))
                && (extraData.isEmpty() || extraData.get().matches(frame.extraData()));
    }
}
