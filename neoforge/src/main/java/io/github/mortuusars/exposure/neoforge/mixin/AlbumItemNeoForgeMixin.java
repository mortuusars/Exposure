package io.github.mortuusars.exposure.neoforge.mixin;

import io.github.mortuusars.exposure.world.item.AbstractAlbumItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Item.class, remap = false)
public abstract class AlbumItemNeoForgeMixin implements IItemExtension {

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        if (((Object) this) instanceof AbstractAlbumItem albumItemCommon) return albumItemCommon.shouldPlayEquipAnimation(oldStack, newStack);
        else return !oldStack.equals(newStack);
    }
}
