package io.github.mortuusars.exposure.integration.jei.recipe;

import io.github.mortuusars.exposure.recipe.AbstractNbtTransferringRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public record NbtTransferringShapelessExtension(AbstractNbtTransferringRecipe recipe) implements ICraftingCategoryExtension {
    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ICraftingGridHelper craftingGridHelper, @NotNull IFocusGroup focuses) {
        List<List<ItemStack>> inputs = recipe.getIngredients().stream()
                .map(ingredient -> List.of(ingredient.getItems()))
                .collect(Collectors.toList());

        inputs.add(0, List.of(recipe.getTransferIngredient().getItems()));

        ItemStack resultItem = recipe.getResult();

        int width = getWidth();
        int height = getHeight();
        craftingGridHelper.createAndSetInputs(builder, inputs, width, height);
        craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
    }
}