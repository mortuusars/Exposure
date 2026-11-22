package io.github.mortuusars.exposure.world.item.crafting.recipe;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhotographAgingRecipe extends ComponentTransferringRecipe {
    public PhotographAgingRecipe(CraftingBookCategory category, Ingredient sourceIngredient,
                                 List<Ingredient> ingredients, ItemStack result) {
        super(category, sourceIngredient, ingredients, result);
    }

    @Override
    public @NotNull RecipeSerializer<? extends ComponentTransferringRecipe> getSerializer() {
        return (RecipeSerializer<? extends ComponentTransferringRecipe>) Exposure.RecipeSerializers.PHOTOGRAPH_AGING.get();
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);

        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof BrushItem) {
                stack = stack.copy();
                int damage = stack.getDamageValue() + 1;
                stack.setDamageValue(damage);
                if (damage >= stack.getMaxDamage()) {
                    stack.shrink(1);
                }
                remainingItems.set(i, stack);
            }
        }

        return remainingItems;
    }
}
