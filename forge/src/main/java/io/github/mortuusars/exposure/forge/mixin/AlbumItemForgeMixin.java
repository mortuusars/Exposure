package io.github.mortuusars.exposure.forge.mixin;

import io.github.mortuusars.exposure.item.AlbumItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AlbumItem.class, remap = false)
public abstract class AlbumItemForgeMixin implements IForgeItem {
    @Shadow
    abstract boolean shouldPlayEquipAnimation(ItemStack oldStack, ItemStack newStack);

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return shouldPlayEquipAnimation(oldStack, newStack);
    }
}
