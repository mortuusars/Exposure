package io.github.mortuusars.exposure.fabric.mixin;

import io.github.mortuusars.exposure.world.item.AbstractAlbumItem;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Item.class, remap = false)
public abstract class AlbumItemFabricMixin implements FabricItem {

    @Override
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        if (((Object) this) instanceof AbstractAlbumItem albumItemCommon) return albumItemCommon.shouldPlayEquipAnimation(oldStack, newStack);
        else return true;
    }
}
