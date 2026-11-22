package io.github.mortuusars.exposure.world.item.crafting.recipe.serializer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ComponentTransferringRecipeSerializer<T extends ComponentTransferringRecipe> implements RecipeSerializer<T> {
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public ComponentTransferringRecipeSerializer(String recipeName, String sourceName, RecipeConstructor<T> constructor) {
        this.codec = createCodec(recipeName, sourceName, constructor);
        this.streamCodec = createStreamCodec(constructor);
    }

    public ComponentTransferringRecipeSerializer(String serializedSourceIngredientName, RecipeConstructor<T> constructor) {
        this("component_transferring", serializedSourceIngredientName, constructor);
    }

    public ComponentTransferringRecipeSerializer(RecipeConstructor<T> constructor) {
        this("component_transferring", "source_ingredient", constructor);
    }

    protected @NotNull MapCodec<T> createCodec(String recipeTypeName, String sourceIngredientName, RecipeConstructor<T> constructor) {
        return RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                        Ingredient.CODEC.fieldOf(sourceIngredientName).forGetter(ComponentTransferringRecipe::getSourceIngredient),
                        Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(ComponentTransferringRecipe::getIngredients),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ComponentTransferringRecipe::getResult)
                ).apply(instance, constructor::create)
        );
    }

    protected @NotNull StreamCodec<RegistryFriendlyByteBuf, T> createStreamCodec(RecipeConstructor<T> constructor) {
        return StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, ComponentTransferringRecipe::getSourceIngredient,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), ComponentTransferringRecipe::getIngredients,
                ItemStack.STREAM_CODEC, ComponentTransferringRecipe::getResult,
                constructor::create);
    }

    @Override
    public @NotNull MapCodec<T> codec() {
        return codec;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return streamCodec;
    }

    @FunctionalInterface
    public interface RecipeConstructor<T extends ComponentTransferringRecipe> {
        T create(CraftingBookCategory arg, Ingredient sourceIngredient, List<Ingredient> ingredients, ItemStack result);
    }
}
