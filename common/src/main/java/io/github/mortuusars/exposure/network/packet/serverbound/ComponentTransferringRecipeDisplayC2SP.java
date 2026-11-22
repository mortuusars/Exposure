package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.clientbound.ComponentTransferringRecipeDisplayResponseS2CP;
import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.FilmDevelopingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.PhotographAgingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.PhotographCopyingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.display.ComponentTransferringRecipeDisplay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public record ComponentTransferringRecipeDisplayC2SP(ItemStack sourceIngredient, String recipeType) implements Packet {
    public static final Identifier ID = Exposure.resource("component_transferring_recipe_display");
    public static final Type<ComponentTransferringRecipeDisplayC2SP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ComponentTransferringRecipeDisplayC2SP> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ComponentTransferringRecipeDisplayC2SP::sourceIngredient,
            ByteBufCodecs.STRING_UTF8, ComponentTransferringRecipeDisplayC2SP::recipeType,
            ComponentTransferringRecipeDisplayC2SP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ServerLevel serverLevel = (ServerLevel) player.level();

        RecipeMap recipeMap = RecipeMap.create(serverLevel.recipeAccess().getRecipes());

        Predicate<CraftingRecipe> filter = getCraftingRecipePredicate();

        Optional<ComponentTransferringRecipeDisplay> display = recipeMap.byType(RecipeType.CRAFTING).stream()
                .map(RecipeHolder::value)
                .filter(filter)
                .findFirst()
                .map(recipe -> (ComponentTransferringRecipeDisplay) recipe.display().getFirst());

        Packets.sendToClient(new ComponentTransferringRecipeDisplayResponseS2CP(display, sourceIngredient), (ServerPlayer) player);

        return true;
    }

    private @NotNull Predicate<CraftingRecipe> getCraftingRecipePredicate() {
        return switch (recipeType) {
            case "developing" -> r -> r instanceof FilmDevelopingRecipe filmDevelopingRecipe
                    && filmDevelopingRecipe.getSourceIngredient().test(sourceIngredient);
            case "copying" -> r -> r instanceof PhotographCopyingRecipe photographCopyingRecipe
                    && photographCopyingRecipe.getSourceIngredient().test(sourceIngredient);
            case "aging" -> r -> r instanceof PhotographAgingRecipe photographAgingRecipe
                    && photographAgingRecipe.getSourceIngredient().test(sourceIngredient);
            default -> r -> false;
        };
    }
}
