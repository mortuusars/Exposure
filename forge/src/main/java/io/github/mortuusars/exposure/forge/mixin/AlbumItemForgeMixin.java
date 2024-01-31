package io.github.mortuusars.exposure.forge.mixin;

import io.github.mortuusars.exposure.item.AlbumItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = AlbumItem.class, remap = false)
public abstract class AlbumItemForgeMixin implements IForgeItem {

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }
}
