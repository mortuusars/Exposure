package io.github.mortuusars.exposure.fabric.mixin.create;

import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.fabric.integration.create.CreateFilmDeveloping;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(value = CreateJEI.class, remap = false)
public abstract class CreateJEICompatMixin {
    @Inject(method = "registerRecipes", at = @At("RETURN"))
    void onRegisterRecipes(IRecipeRegistration registration, CallbackInfo ci) {
        registration.addRecipes(new RecipeType<>(Create.asResource("sequenced_assembly"), SequencedAssemblyRecipe.class),
                List.of(exposure$developingRecipe(FilmType.BLACK_AND_WHITE),
                        exposure$developingRecipe(FilmType.COLOR)));
    }

    @Unique
    private SequencedAssemblyRecipe exposure$developingRecipe(FilmType filmType) {
        List<FluidStack> fillingSteps = CreateFilmDeveloping.getFillingSteps(filmType);

        SequencedAssemblyRecipeBuilder recipeBuilder = new SequencedAssemblyRecipeBuilder(Exposure.resource("sequenced_" + filmType.getSerializedName() + "_film_developing"))
                .require(filmType.createItemStack().getItem())
                .transitionTo(filmType.createItemStack().getItem())
                .loops(1)
                .addOutput(filmType.createDevelopedItemStack().getItem(), 1);

        for (FluidStack fluidStack : fillingSteps) {
            recipeBuilder.addStep(FillingRecipe::new, f -> f.require(FluidIngredient.fromFluidStack(fluidStack)));
        }

        return recipeBuilder.build();
    }
}
