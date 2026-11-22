package io.github.mortuusars.exposure.world.item.crafting.recipe.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public record ComponentTransferringRecipeDisplay(SlotDisplay sourceIngredient, List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {
    public static final MapCodec<ComponentTransferringRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlotDisplay.CODEC.fieldOf("sourceIngredient").forGetter(ComponentTransferringRecipeDisplay::sourceIngredient),
                SlotDisplay.CODEC.listOf().fieldOf("ingredient").forGetter(ComponentTransferringRecipeDisplay::ingredients),
                SlotDisplay.CODEC.fieldOf("result").forGetter(ComponentTransferringRecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(ComponentTransferringRecipeDisplay::craftingStation)
            )
            .apply(instance, ComponentTransferringRecipeDisplay::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ComponentTransferringRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC,
            ComponentTransferringRecipeDisplay::sourceIngredient,
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ComponentTransferringRecipeDisplay::ingredients,
            SlotDisplay.STREAM_CODEC,
            ComponentTransferringRecipeDisplay::result,
            SlotDisplay.STREAM_CODEC,
            ComponentTransferringRecipeDisplay::craftingStation,
            ComponentTransferringRecipeDisplay::new
    );
    public static final RecipeDisplay.Type<ComponentTransferringRecipeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public Type<? extends RecipeDisplay> type() {
        return null;
    }
}
