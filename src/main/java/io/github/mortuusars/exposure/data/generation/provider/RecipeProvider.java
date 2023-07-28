package io.github.mortuusars.exposure.data.generation.provider;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    public RecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> recipeConsumer) {
//        ItemStack waterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);

//        CompoundTag tag = new CompoundTag();
//        tag.putString("Potion", ForgeRegistries.POTIONS.getKey(Potions.WATER).toString());
//        PartialNBTIngredient waterBottle = PartialNBTIngredient.of(Items.POTION, tag);
//
//        ShapelessRecipeBuilder.shapeless(Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get())
//                .requires(Exposure.Items.BLACK_AND_WHITE_FILM_ROLL.get())
//                .requires(waterBottle)
//                .requires(Items.GUNPOWDER)
//                .unlockedBy("has_film_roll", has(Exposure.Items.BLACK_AND_WHITE_FILM_ROLL.get()))
//                .save(recipeConsumer, Exposure.resource("developing_black_and_white_film"));
//
////        ItemStack awkwardPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
////        ItemStack mundanePotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.MUNDANE);
////        ItemStack thickPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.THICK);
//
//        CompoundTag awkwardPotionTag = new CompoundTag();
//        awkwardPotionTag.putString("Potion", ForgeRegistries.POTIONS.getKey(Potions.AWKWARD).toString());
//        PartialNBTIngredient awkwardPotion = PartialNBTIngredient.of(Items.POTION, awkwardPotionTag);
//
//        CompoundTag mundanePotionTag = new CompoundTag();
//        mundanePotionTag.putString("Potion", ForgeRegistries.POTIONS.getKey(Potions.MUNDANE).toString());
//        PartialNBTIngredient mundanePotion = PartialNBTIngredient.of(Items.POTION, mundanePotionTag);
//
//        CompoundTag thickPotionTag = new CompoundTag();
//        thickPotionTag.putString("Potion", ForgeRegistries.POTIONS.getKey(Potions.THICK).toString());
//        PartialNBTIngredient thickPotion = PartialNBTIngredient.of(Items.POTION, thickPotionTag);
//
//        new ShapelessRecipeBuilder(Exposure.Items.DEVELOPED_COLOR_FILM.get(), 1) {
//        }
//                .requires(Exposure.Items.COLOR_FILM_ROLL.get())
//                .requires(awkwardPotion)
//                .requires(mundanePotion)
//                .requires(thickPotion)
//                .unlockedBy("has_film_roll", has(Exposure.Items.COLOR_FILM_ROLL.get()))
//                .save(recipeConsumer, Exposure.resource("developing_color_film"));
    }
}
