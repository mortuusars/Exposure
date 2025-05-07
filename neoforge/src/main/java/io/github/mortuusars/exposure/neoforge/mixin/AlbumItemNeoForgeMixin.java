package io.github.mortuusars.exposure.neoforge.mixin;

import io.github.mortuusars.exposure.world.item.AbstractAlbumItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IItemExtension.class, remap = false)
public abstract class AlbumItemNeoForgeMixin implements IItemExtension {

    @Inject(method = "shouldCauseReequipAnimation", at = @At("HEAD"), cancellable = true)
    public void shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged, CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this) instanceof AbstractAlbumItem albumItemCommon) cir.setReturnValue(albumItemCommon.shouldPlayEquipAnimation(oldStack, newStack));
    }
}
