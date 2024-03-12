package io.github.mortuusars.exposure.fabric.mixin.create;

import com.simibubi.create.content.fluids.spout.FillingBySpout;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.fabric.integration.create.CreateFilmDeveloping;
import io.github.mortuusars.exposure.item.FilmRollItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = FillingBySpout.class, remap = false)
public abstract class SpoutDevelopingMixin {
    @Inject(method = "canItemBeFilled", at = @At("HEAD"), cancellable = true)
    private static void onCanItemBeFilled(Level world, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (Config.Common.CREATE_SPOUT_DEVELOPING_ENABLED.get() && stack.getItem() instanceof FilmRollItem)
            cir.setReturnValue(true);
    }

    @Inject(method = "getRequiredAmountForItem", at = @At("HEAD"), cancellable = true)
    private static void onGetRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<Long> cir) {
        if (Config.Common.CREATE_SPOUT_DEVELOPING_ENABLED.get() && stack.getItem() instanceof FilmRollItem) {
            @Nullable FluidStack nextFluidStep = CreateFilmDeveloping.getNextRequiredFluid(stack);
            if (nextFluidStep == null)
                cir.setReturnValue(0L);
            else if (FluidIngredient.fromFluidStack(nextFluidStep).test(availableFluid))
                cir.setReturnValue(nextFluidStep.getAmount());
        }
    }

    @Inject(method = "fillItem", at = @At("HEAD"), cancellable = true)
    private static void onFillItem(Level world, long requiredAmount, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
        if (Config.Common.CREATE_SPOUT_DEVELOPING_ENABLED.get() && stack.getItem() instanceof FilmRollItem) {
            ItemStack result = CreateFilmDeveloping.fillFilmStack(stack, requiredAmount, availableFluid);
            cir.setReturnValue(result);
        }
    }
}
