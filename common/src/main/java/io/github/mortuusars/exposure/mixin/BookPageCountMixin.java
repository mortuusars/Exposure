package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.item.AlbumItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WrittenBookItem.class)
public abstract class BookPageCountMixin {
    @Inject(method = "getPageCount", at = @At("HEAD"), cancellable = true)
    private static void getPageCount(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof AlbumItem albumItem) {
            cir.setReturnValue(albumItem.getPages(stack).size());
        }
    }
}
