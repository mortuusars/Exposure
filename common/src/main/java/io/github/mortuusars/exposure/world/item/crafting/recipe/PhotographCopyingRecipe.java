package io.github.mortuusars.exposure.world.item.crafting.recipe;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhotographCopyingRecipe extends ComponentTransferringRecipe {
    public PhotographCopyingRecipe(CraftingBookCategory category, Ingredient sourceIngredient, List<Ingredient> ingredients, ItemStack result) {
        super(category, sourceIngredient, ingredients, result);
    }

    @Override
    public @NotNull RecipeSerializer<? extends ComponentTransferringRecipe> getSerializer() {
        return (RecipeSerializer<? extends ComponentTransferringRecipe>) Exposure.RecipeSerializers.PHOTOGRAPH_COPYING.get();
    }

    @Override
    public @NotNull ItemStack transferComponents(ItemStack stack, ItemStack recipeResultStack) {
        int generation = stack.getOrDefault(Exposure.DataComponents.PHOTOGRAPH_GENERATION, 0);
        if (generation < 2) {
            ItemStack result = super.transferComponents(stack, recipeResultStack);
            result.set(Exposure.DataComponents.PHOTOGRAPH_GENERATION, generation + 1);
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);;

        for(int i = 0; i < remainingItems.size(); ++i) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof PhotographItem) {
                ItemStack remainingPhotographStack = stack.copy();
                remainingPhotographStack.setCount(1);
                remainingItems.set(i, remainingPhotographStack);
            }
        }

        return remainingItems;
    }
}
