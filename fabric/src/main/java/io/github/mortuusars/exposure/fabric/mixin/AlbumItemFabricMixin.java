package io.github.mortuusars.exposure.fabric.mixin;

import io.github.mortuusars.exposure.item.AlbumItem;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AlbumItem.class, remap = false)
public abstract class AlbumItemFabricMixin implements FabricItem {
    @Shadow
    abstract boolean shouldPlayEquipAnimation(ItemStack oldStack, ItemStack newStack);
    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return shouldPlayEquipAnimation(oldStack, newStack);
    }
}
