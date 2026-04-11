package io.github.mortuusars.exposure.client.render.item.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.client.render.item.CameraStatus;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record StackCameraStatus() implements SelectItemModelProperty<CameraStatus> {
    public static final SelectItemModelProperty.Type<StackCameraStatus, CameraStatus> TYPE = SelectItemModelProperty.Type.create(
            MapCodec.unit(new StackCameraStatus()), CameraStatus.CODEC
    );

    @Override
    public @Nullable CameraStatus get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        if (stack.getItem() instanceof CameraItem cameraItem) {
            boolean active = cameraItem.isActive(stack);
            boolean selfie = cameraItem.isInSelfieMode(stack);
            if (selfie) {
                if (entity == Minecrft.get().getCameraEntity()) {
                    return CameraStatus.SELFIE_VIEWFINDER;
                } else {
                    return CameraStatus.SELFIE;
                }
            } else if (active)
                return CameraStatus.ACTIVE;
            else
                return CameraStatus.NONE;
        }
        return null;
    }

    @Override
    public Type<? extends SelectItemModelProperty<CameraStatus>, CameraStatus> type() {
        return TYPE;
    }

    @Override
    public Codec<CameraStatus> valueCodec() {
        return CameraStatus.CODEC;
    }
}
