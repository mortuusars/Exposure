package io.github.mortuusars.exposure.integration.jei;

import com.google.common.collect.ImmutableList;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.integration.jei.category.PhotographPrintingCategory;
import io.github.mortuusars.exposure.integration.jei.category.PhotographStackingCategory;
import io.github.mortuusars.exposure.integration.jei.recipe.NbtTransferringShapelessExtension;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographPrintingJeiRecipe;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographStackingJeiRecipe;
import io.github.mortuusars.exposure.recipe.FilmDevelopingRecipe;
import io.github.mortuusars.exposure.recipe.PhotographAgingRecipe;
import io.github.mortuusars.exposure.recipe.PhotographCopyingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory()
                .addCategoryExtension(FilmDevelopingRecipe.class, NbtTransferringShapelessExtension::new);
        registration.getCraftingCategory()
                .addCategoryExtension(PhotographCopyingRecipe.class, NbtTransferringShapelessExtension::new);
        registration.getCraftingCategory()
                .addCategoryExtension(PhotographAgingRecipe.class, NbtTransferringShapelessExtension::new);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AlbumScreen.class, new IGuiContainerHandler<AlbumScreen>() {
            @Override
            public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull AlbumScreen containerScreen) {
                return List.of(new Rect2i(0, 0,
                        Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                        Minecraft.getInstance().getWindow().getGuiScaledHeight()));
            }
        });
    }
}