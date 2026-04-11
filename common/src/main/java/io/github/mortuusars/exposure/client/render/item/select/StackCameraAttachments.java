package io.github.mortuusars.exposure.client.render.item.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.client.render.item.CameraAttachments;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record StackCameraAttachments() implements SelectItemModelProperty<CameraAttachments> {
    public static final SelectItemModelProperty.Type<StackCameraAttachments, CameraAttachments> TYPE = SelectItemModelProperty.Type.create(
            MapCodec.unit(new StackCameraAttachments()), CameraAttachments.CODEC
    );

    @Override
    public @Nullable CameraAttachments get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        if (stack.getItem() instanceof CameraItem) {
            boolean hasFlash = Attachment.FLASH.isPresent(stack);
            boolean hasLens = Attachment.LENS.isPresent(stack);

            if (hasFlash && hasLens)
                return CameraAttachments.LENS_AND_FLASH;
            else if (hasFlash)
                return CameraAttachments.FLASH;
            else if (hasLens)
                return CameraAttachments.LENS;
            else
                return CameraAttachments.NONE;
        }
        return null;
    }

    @Override
    public Type<? extends SelectItemModelProperty<CameraAttachments>, CameraAttachments> type() {
        return TYPE;
    }

    @Override
    public Codec<CameraAttachments> valueCodec() {
        return CameraAttachments.CODEC;
    }
}
