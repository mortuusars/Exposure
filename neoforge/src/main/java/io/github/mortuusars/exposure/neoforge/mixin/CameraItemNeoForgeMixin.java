package io.github.mortuusars.exposure.neoforge.mixin;

import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CameraItem.class)
public abstract class CameraItemNeoForgeMixin extends Item implements IItemExtension {
    public CameraItemNeoForgeMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSameItemSameComponents(newStack, oldStack);
    }
}
