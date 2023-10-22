package io.github.mortuusars.exposure.integration.jei;

import com.google.common.collect.ImmutableList;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.integration.jei.category.PhotographPrintingCategory;
import io.github.mortuusars.exposure.integration.jei.category.PhotographStackingCategory;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographPrintingJeiRecipe;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographStackingJeiRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class ExposureJeiPlugin implements IModPlugin {
    public static final RecipeType<PhotographPrintingJeiRecipe> PHOTOGRAPH_PRINTING_RECIPE_TYPE =
            RecipeType.create(Exposure.ID, "photograph_printing", PhotographPrintingJeiRecipe.class);
    public static final RecipeType<PhotographStackingJeiRecipe> PHOTOGRAPH_STACKING_RECIPE_TYPE =
            RecipeType.create(Exposure.ID, "photograph_stacking", PhotographStackingJeiRecipe.class);

    private static final ResourceLocation ID = Exposure.resource("jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PhotographPrintingCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new PhotographStackingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Exposure.Items.LIGHTROOM.get()), PHOTOGRAPH_PRINTING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Exposure.Items.STACKED_PHOTOGRAPHS.get()), PHOTOGRAPH_STACKING_RECIPE_TYPE);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        registration.addRecipes(PHOTOGRAPH_PRINTING_RECIPE_TYPE, ImmutableList.of(
                new PhotographPrintingJeiRecipe(FilmType.BLACK_AND_WHITE),
                new PhotographPrintingJeiRecipe(FilmType.COLOR)
        ));

        registration.addRecipes(PHOTOGRAPH_STACKING_RECIPE_TYPE, ImmutableList.of(
                new PhotographStackingJeiRecipe(PhotographStackingJeiRecipe.STACKING),
                new PhotographStackingJeiRecipe(PhotographStackingJeiRecipe.REMOVING)
        ));
    }
}