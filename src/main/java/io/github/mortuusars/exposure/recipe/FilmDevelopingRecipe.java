package io.github.mortuusars.exposure.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.FilmRollItem;
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
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

public class FilmDevelopingRecipe extends ShapelessRecipe {
    public FilmDevelopingRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, result, ingredients);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(container);
//        NonNullList<ItemStack> remainingItems = NonNullList.withSize(items.size() + container.getContainerSize(), ItemStack.EMPTY) ;
//        remainingItems.addAll(items);

        for(int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack item = container.getItem(i);
            if (item.getItem() instanceof PotionItem) {
                remainingItems.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        return remainingItems;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Exposure.RecipeSerializers.FILM_DEVELOPING.get();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer container) {
        for (int index = 0; index < container.getContainerSize(); index++) {
            ItemStack itemStack = container.getItem(index);

            if (itemStack.getItem() instanceof FilmRollItem filmRollItem) {
                return filmRollItem.develop(itemStack).getStack();
            }
        }

        return ItemStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<FilmDevelopingRecipe> {
        public @NotNull FilmDevelopingRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new FilmDevelopingRecipe(recipeId, group, result, getIngredients(json));
        }

        private NonNullList<Ingredient> getIngredients(JsonObject json) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();

            for(int i = 0; i < jsonArray.size(); ++i) {
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

        public FilmDevelopingRecipe fromNetwork(@NotNull ResourceLocation recipeID, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            ItemStack result = buffer.readItem();
            int ingredientsCount = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsCount, Ingredient.EMPTY);

            //noinspection Java8ListReplaceAll
            for(int i = 0; i < ingredients.size(); ++i) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }

            return new FilmDevelopingRecipe(recipeID, group, result, ingredients);
        }

        public void toNetwork(FriendlyByteBuf buffer, FilmDevelopingRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeItemStack(recipe.getResultItem(), false);
            buffer.writeVarInt(recipe.getIngredients().size());

            for(Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
        }
    }
}
