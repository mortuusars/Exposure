package io.github.mortuusars.exposure.entity.forge;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;

public class PhotographEntityImpl {
    public static boolean canShear(ItemStack stack) {
        return stack.canPerformAction(ToolActions.SHEARS_CARVE);
    }
}
