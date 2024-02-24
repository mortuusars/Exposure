package io.github.mortuusars.exposure.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FilmDevelopingRecipe extends AbstractNbtTransferringRecipe {
    public FilmDevelopingRecipe(ResourceLocation id, Ingredient filmIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(id, filmIngredient, ingredients, result);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Exposure.RecipeSerializers.FILM_DEVELOPING.get();
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer container) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(container);

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack item = container.getItem(i);
            if (item.getItem() instanceof PotionItem) {
                remainingItems.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
            else if (item.getItem().hasCraftingRemainingItem()) {
                remainingItems.set(i, new ItemStack(Objects.requireNonNull(item.getItem().getCraftingRemainingItem())));
            }
        }

        return remainingItems;
    }

    public static class Serializer implements RecipeSerializer<FilmDevelopingRecipe> {
        @Override
        public @NotNull FilmDevelopingRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            Ingredient filmIngredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(serializedRecipe, "film"));
            NonNullList<Ingredient> ingredients = getIngredients(GsonHelper.getAsJsonArray(serializedRecipe, "ingredients"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe, "result"));

            if (filmIngredient.isEmpty())
                throw new JsonParseException("Recipe should have 'film' ingredient.");

            return new FilmDevelopingRecipe(recipeId, filmIngredient, ingredients, result);
        }

        @Override
        public @NotNull FilmDevelopingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient transferredIngredient = Ingredient.fromNetwork(buffer);
            int ingredientsCount = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsCount, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();

            return new FilmDevelopingRecipe(recipeId, transferredIngredient, ingredients, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FilmDevelopingRecipe recipe) {
            recipe.getTransferIngredient().toNetwork(buffer);
            buffer.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.getResult());
        }

        private NonNullList<Ingredient> getIngredients(JsonArray jsonArray) {
            NonNullList<Ingredient> ingredients = NonNullList.create();

            for (int i = 0; i < jsonArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(jsonArray.get(i));
                if (!ingredient.isEmpty())
                    ingredients.add(ingredient);
            }

            if (ingredients.isEmpty())
                throw new JsonParseException("No ingredients for a recipe.");
            else if (ingredients.size() > 3 * 3)
                throw new JsonParseException("Too many ingredients for a recipe. The maximum is 9.");
            return ingredients;
        }
    }
}
