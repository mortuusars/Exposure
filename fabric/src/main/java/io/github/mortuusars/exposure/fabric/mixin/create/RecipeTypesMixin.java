package io.github.mortuusars.exposure.fabric.mixin.create;

import com.simibubi.create.AllRecipeTypes;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = AllRecipeTypes.class, remap = false)
public class RecipeTypesMixin {
    @Inject(method = "shouldIgnoreInAutomation", at = @At("HEAD"), cancellable = true)
    private static void onShouldIgnoreInAutomation(Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (recipe.getSerializer().equals(Exposure.RecipeSerializers.FILM_DEVELOPING.get()) ||
                recipe.getSerializer().equals(Exposure.RecipeSerializers.PHOTOGRAPH_CLONING.get()))
            cir.setReturnValue(true);
    }
}
