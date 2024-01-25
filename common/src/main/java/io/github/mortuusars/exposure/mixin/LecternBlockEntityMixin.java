package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.item.AlbumItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin {
//    @Shadow private int pageCount;
//
//    @Inject(method = "setBook(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V",
//        at = @At(target = "Lnet/minecraft/world/level/block/entity/LecternBlockEntity;setChanged()V", value = "INVOKE"))
//    private void onSetBook(ItemStack stack, Player player, CallbackInfo ci) {
//        if (stack.getItem() instanceof AlbumItem albumItem) {
//            this.pageCount = albumItem.getPages(stack).size() / 2; // Spreads
//        }
//    }
}
