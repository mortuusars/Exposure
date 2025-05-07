package io.github.mortuusars.exposure.fabric.mixin;

import io.github.mortuusars.exposure.world.item.AbstractAlbumItem;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FabricItem.class, remap = false)
public abstract class AlbumItemFabricMixin implements FabricItem {

    @Inject(method = "allowComponentsUpdateAnimation", at = @At("HEAD"), cancellable = true)
    public void allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack, CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this) instanceof AbstractAlbumItem albumItemCommon) cir.setReturnValue(albumItemCommon.shouldPlayEquipAnimation(oldStack, newStack));
    }
}
