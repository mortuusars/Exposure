package io.github.mortuusars.exposure.data.generation.provider;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.generation.recipe.FilmDevelopingFinishedRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    public RecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> recipeConsumer) {
        ResourceLocation bwRecipeId = Exposure.resource("developing_black_and_white_film_roll");
        Advancement.Builder bwAdvancementBuilder = Advancement.Builder.advancement()
                .parent(new ResourceLocation("recipes/root"))
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(bwRecipeId))
                .addCriterion("has_black_and_white_film", has(Exposure.Items.BLACK_AND_WHITE_FILM_ROLL.get()))
                .rewards(AdvancementRewards.Builder.recipe(bwRecipeId))
                .requirements(RequirementsStrategy.OR);

        recipeConsumer.accept(new FilmDevelopingFinishedRecipe(bwRecipeId,
                Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM_ROLL.get(), 1, "",
                List.of(Ingredient.of(Exposure.Items.BLACK_AND_WHITE_FILM_ROLL.get()), potionIngredient(Potions.WATER), Ingredient.of(Items.FERMENTED_SPIDER_EYE)), bwAdvancementBuilder,
                new ResourceLocation(bwRecipeId.getNamespace(), "recipes/" +
                        Objects.requireNonNull(Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM_ROLL.get()
                                .getItemCategory()).getRecipeFolderName() + "/" + bwRecipeId.getPath())
        ));

        ResourceLocation colorRecipeId = Exposure.resource("developing_color_film_roll");
        Advancement.Builder colorAdvancementBuilder = Advancement.Builder.advancement()
                .parent(new ResourceLocation("recipes/root"))
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(colorRecipeId))
                .addCriterion("has_color_film", has(Exposure.Items.COLOR_FILM_ROLL.get()))
                .rewards(AdvancementRewards.Builder.recipe(colorRecipeId))
                .requirements(RequirementsStrategy.OR);

        recipeConsumer.accept(new FilmDevelopingFinishedRecipe(colorRecipeId,
                Exposure.Items.DEVELOPED_COLOR_FILM_ROLL.get(), 1, "",
                List.of(Ingredient.of(Exposure.Items.COLOR_FILM_ROLL.get()), potionIngredient(Potions.AWKWARD), potionIngredient(Potions.MUNDANE), potionIngredient(Potions.THICK)), colorAdvancementBuilder,
                new ResourceLocation(colorRecipeId.getNamespace(), "recipes/" +
                        Objects.requireNonNull(Exposure.Items.DEVELOPED_COLOR_FILM_ROLL.get()
                                .getItemCategory()).getRecipeFolderName() + "/" + colorRecipeId.getPath())
        ));


        ShapedRecipeBuilder.shaped(Exposure.Items.BLACK_AND_WHITE_FILM_ROLL.get())
                .pattern("NBB")
                .pattern("IGG")
                .pattern("IKK")
                .define('N', Tags.Items.NUGGETS_IRON)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('B', Items.BONE_MEAL)
                .define('G', Items.GUNPOWDER)
                .define('K', Items.DRIED_KELP)
                .unlockedBy("has_camera", has(Exposure.Items.CAMERA.get()))
                .save(recipeConsumer);

        ShapedRecipeBuilder.shaped(Exposure.Items.COLOR_FILM_ROLL.get())
                .pattern("NLL")
                .pattern("IGG")
                .pattern("IKK")
                .define('N', Tags.Items.NUGGETS_IRON)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('L', Items.LAPIS_LAZULI)
                .define('G', Tags.Items.NUGGETS_GOLD)
                .define('K', Items.DRIED_KELP)
                .unlockedBy("has_camera", has(Exposure.Items.CAMERA.get()))
                .save(recipeConsumer);
    }

    private Ingredient potionIngredient(Potion potion) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Potion", Objects.requireNonNull(ForgeRegistries.POTIONS.getKey(potion)).toString());
        return PartialNBTIngredient.of(Items.POTION, tag);
    }
}
