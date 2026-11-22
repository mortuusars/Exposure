package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.world.item.BrokenInterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ItemCombinerScreen<AnvilMenu> {
    @Shadow private EditBox name;

    public AnvilScreenMixin(AnvilMenu menu, Inventory playerInventory, Component title, Identifier menuResource) {
        super(menu, playerInventory, title, menuResource);
    }

    @Inject(method = "slotChanged", at = @At("HEAD"))
    private void onSlotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack, CallbackInfo ci) {
        if (dataSlotIndex == 0 && Config.Server.INTERPLANAR_PROJECTOR_LARGER_RENAMING_LIMIT.isTrue()) {
            int maxLength = stack.getItem() instanceof InterplanarProjectorItem || stack.getItem() instanceof BrokenInterplanarProjectorItem ? 150 : 50;
            this.name.setMaxLength(maxLength);
        }
    }
}
