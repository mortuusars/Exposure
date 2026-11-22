package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record CameraPredicate(Optional<ItemPredicate> camera,
                              Optional<ItemPredicate> film,
                              Optional<ItemPredicate> flash,
                              Optional<ItemPredicate> lens,
                              Optional<ItemPredicate> filter,
                              Optional<LocationPredicate> location) {

    public static final Codec<CameraPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemPredicate.CODEC.optionalFieldOf("camera").forGetter(CameraPredicate::camera),
                    ItemPredicate.CODEC.optionalFieldOf("film").forGetter(CameraPredicate::film),
                    ItemPredicate.CODEC.optionalFieldOf("flash").forGetter(CameraPredicate::flash),
                    ItemPredicate.CODEC.optionalFieldOf("lens").forGetter(CameraPredicate::lens),
                    ItemPredicate.CODEC.optionalFieldOf("filter").forGetter(CameraPredicate::filter),
                    LocationPredicate.CODEC.optionalFieldOf("location").forGetter(CameraPredicate::location))
            .apply(instance, CameraPredicate::new));

    public boolean matches(ServerLevel level, ItemStack cameraStack, Vec3 cameraLocation) {
        if (!(cameraStack.getItem() instanceof CameraItem)) return false;

        return (camera.isEmpty() || camera.get().test(cameraStack))
                && (film.isEmpty() || film.get().test(Attachment.FILM.get(cameraStack).getForReading()))
                && (flash.isEmpty() || flash.get().test(Attachment.FLASH.get(cameraStack).getForReading()))
                && (lens.isEmpty() || lens.get().test(Attachment.LENS.get(cameraStack).getForReading()))
                && (filter.isEmpty() || filter.get().test(Attachment.FILTER.get(cameraStack).getForReading()))
                && (location.isEmpty() || location.get().matches(level, cameraLocation.x, cameraLocation.y, cameraLocation.z));
    }
}
