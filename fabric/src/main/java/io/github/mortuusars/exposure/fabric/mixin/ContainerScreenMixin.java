package io.github.mortuusars.exposure.fabric.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin {
    @Shadow @Nullable protected abstract Slot findSlot(double mouseX, double mouseY);

    /**
     * Fixes strange thing with items dropping if clicked on a slot thats outside of the gui.
     * Forge has this fixed by default.
     */
    @ModifyVariable(require = 0, method = "mouseClicked", at = @At(value = "STORE"), ordinal = 1)
    boolean handleOutsideSlotClicked(boolean clickedOutside, double mouseX, double mouseY, int button) {
        if (clickedOutside && findSlot(mouseX, mouseY) != null)
            return false;

        return clickedOutside;
    }
}
