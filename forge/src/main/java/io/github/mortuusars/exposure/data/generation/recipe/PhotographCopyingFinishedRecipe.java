package io.github.mortuusars.exposure.data.generation.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * "Copy" of the ShapelessRecipeBuilder.Result class to change serializer to PHOTOGRAPH_CLONING. Needed to datagen film developing recipes.
 */
public class PhotographCopyingFinishedRecipe implements FinishedRecipe {
    private final ResourceLocation id;
    private final Item result;
    private final int count;
    private final String group;
    private final List<Ingredient> ingredients;
    private final Advancement.Builder advancement;
    private final ResourceLocation advancementId;

    public PhotographCopyingFinishedRecipe(ResourceLocation id, Item result, int count, String group, List<Ingredient> ingredients,
                                           Advancement.Builder advancement, ResourceLocation advancementId) {
        this.id = id;
        this.result = result;
        this.count = count;
        this.group = group;
        this.ingredients = ingredients;
        this.advancement = advancement;
        this.advancementId = advancementId;
    }

    public void serializeRecipeData(@NotNull JsonObject pJson) {
        if (!this.group.isEmpty()) {
            pJson.addProperty("group", this.group);
        }

        JsonArray jsonarray = new JsonArray();

        for(Ingredient ingredient : this.ingredients) {
            jsonarray.add(ingredient.toJson());
        }

        pJson.add("ingredients", jsonarray);
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.result)).toString());
        if (this.count > 1) {
            jsonobject.addProperty("count", this.count);
        }

        pJson.add("result", jsonobject);
    }

    public @NotNull RecipeSerializer<?> getType() {
        return Exposure.RecipeSerializers.PHOTOGRAPH_CLONING.get();
    }

    /**
     * Gets the ID for the recipe.
     */
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    /**
     * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
     */
    @javax.annotation.Nullable
    public JsonObject serializeAdvancement() {
        return this.advancement.serializeToJson();
    }

    /**
     * Gets the ID for the advancement associated with this recipe.
     */
    @javax.annotation.Nullable
    public ResourceLocation getAdvancementId() {
        return this.advancementId;
    }
}
