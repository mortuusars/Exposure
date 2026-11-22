package io.github.mortuusars.exposure.world.item.crafting.recipe;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.crafting.recipe.display.ComponentTransferringRecipeDisplay;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ComponentTransferringRecipe extends CustomRecipe {
    private final Ingredient sourceIngredient;
    private final List<Ingredient> ingredients;
    private final ItemStack result;
    private PlacementInfo placementInfo;

    public ComponentTransferringRecipe(CraftingBookCategory category, Ingredient sourceIngredient, List<Ingredient> ingredients, ItemStack result) {
        super(category);
        this.sourceIngredient = sourceIngredient;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public @NotNull RecipeSerializer<? extends ComponentTransferringRecipe> getSerializer() {
        return (RecipeSerializer<? extends ComponentTransferringRecipe>) Exposure.RecipeSerializers.COMPONENT_TRANSFERRING.get();
    }

    @Override
    public @NotNull List<RecipeDisplay> display() {
        return List.of(
                new ComponentTransferringRecipeDisplay(
                        sourceIngredient.display(),
                        ingredients.stream().map(Ingredient::display).toList(),
                        new SlotDisplay.ItemStackSlotDisplay(result),
                        new SlotDisplay.ItemSlotDisplay(Exposure.Items.LIGHTROOM.get())
                )
        );
    }

    public @NotNull Ingredient getSourceIngredient() {
        return sourceIngredient;
    }

    public @NotNull List<Ingredient> getIngredients() {
        return ingredients;
    }

    public @NotNull ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (getSourceIngredient().isEmpty() || ingredients.isEmpty())
            return false;

        List<Ingredient> unmatchedIngredients = new ArrayList<>(ingredients);
        unmatchedIngredients.addFirst(getSourceIngredient());

        int itemsInCraftingGrid = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty())
                itemsInCraftingGrid++;

            if (itemsInCraftingGrid > ingredients.size() + 1)
                return false;

            if (!unmatchedIngredients.isEmpty()) {
                for (int j = 0; j < unmatchedIngredients.size(); j++) {
                    if (unmatchedIngredients.get(j).test(stack)) {
                        unmatchedIngredients.remove(j);
                        break;
                    }
                }
            }
        }

        return unmatchedIngredients.isEmpty() && itemsInCraftingGrid == ingredients.size() + 1;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (int index = 0; index < input.size(); index++) {
            ItemStack itemStack = input.getItem(index);

            if (getSourceIngredient().test(itemStack)) {
                return transferComponents(itemStack, getResult().copy());
            }
        }

        return getResult();
    }

    public @NotNull ItemStack transferComponents(ItemStack transferIngredientStack, ItemStack recipeResultStack) {
        // We don't want to keep the item name, item model, or stack size of the source ingredient.
        DataComponentMap components = transferIngredientStack.getComponents()
                .filter(dataComponentType -> !(
                        dataComponentType.equals(DataComponents.ITEM_NAME)
                        || dataComponentType.equals(DataComponents.ITEM_MODEL)
                        || dataComponentType.equals(DataComponents.MAX_STACK_SIZE)
                        ));

        recipeResultStack.applyComponents(components);
        return recipeResultStack;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        if (placementInfo == null) {
            placementInfo = PlacementInfo.create(ingredients);
        }

        return placementInfo;
    }
}
