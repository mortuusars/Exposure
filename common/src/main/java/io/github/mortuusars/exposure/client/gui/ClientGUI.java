package io.github.mortuusars.exposure.client.gui;

import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumViewScreen;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ComponentTransferringRecipeDisplayC2SP;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.crafting.recipe.FilmDevelopingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.PhotographCopyingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.display.ComponentTransferringRecipeDisplay;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClientGUI {
    public static Map<Item, Optional<ComponentTransferringRecipeDisplay>> recipeDisplays;

    public static void openPhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
        Minecrft.get().setScreen(new PhotographScreen(photographs));
    }

    public static void openPhotographsScreenFromItem(int item) {
        Minecrft.get().setScreen(new PhotographScreen(PhotographScreen.PhotographProvider.fromPhotographItem(item)));
    }

    public static void openAlbumViewScreen(ItemStack albumStack) {
        Minecrft.get().setScreen(new AlbumViewScreen(AlbumViewScreen.AlbumAccess.fromItem(albumStack)));
    }

    public static void addFilmRollDevelopingTooltip(ItemStack filmStack, Item.TooltipContext tooltipContext,
                                                    @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        addRecipeTooltip(filmStack, tooltipContext, tooltipComponents, isAdvanced,
                r -> r instanceof FilmDevelopingRecipe filmDevelopingRecipe
                        && filmDevelopingRecipe.getSourceIngredient().test(filmStack),
                "item.exposure.film_roll.tooltip.details.develop", "developing");
    }

    public static void addPhotographCopyingTooltip(ItemStack photographStack, Item.TooltipContext tooltipContext,
                                                   @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        addRecipeTooltip(photographStack, tooltipContext, tooltipComponents, isAdvanced,
                r -> r instanceof PhotographCopyingRecipe photographCopyingRecipe
                        && photographCopyingRecipe.getSourceIngredient().test(photographStack),
                "item.exposure.photograph.tooltip.details.copy", "copying");
    }

    private static void addRecipeTooltip(ItemStack stack, Item.TooltipContext tooltipContext,
                                         @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced,
                                         Predicate<CraftingRecipe> recipeFilter, String detailsKey, String recipeType) {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        tooltipComponents.accept(Component.translatable("tooltip.exposure.hold_for_details"));
        if (!Minecraft.getInstance().hasShiftDown()) {
            return;
        }

        if (recipeDisplays == null) {
            recipeDisplays = new HashMap<>();
            recipeDisplays.put(stack.getItem(), Optional.empty());
            Packets.sendToServer(new ComponentTransferringRecipeDisplayC2SP(stack, recipeType));
            return;
        }

        @Nullable
        Optional<ComponentTransferringRecipeDisplay> displayOptional = recipeDisplays.get(stack.getItem());
        if (displayOptional == null) { // we haven't requested this recipe yet.
            recipeDisplays.put(stack.getItem(), Optional.empty());
            Packets.sendToServer(new ComponentTransferringRecipeDisplayC2SP(stack, recipeType));
            return;
        }

        if (displayOptional.isEmpty()) { // we have requested this recipe, but we haven't gotten a response.
            return;
        }

        ComponentTransferringRecipeDisplay display = displayOptional.get();

        tooltipComponents.accept(Component.empty());

        Style orange = Style.EMPTY.withColor(0xc7954b);
        Style yellow = Style.EMPTY.withColor(0xeeda78);

        tooltipComponents.accept(Component.translatable(detailsKey).withStyle(orange));

        List<SlotDisplay> ingredientDisplays = display.ingredients();


        for (int i = 0; i < ingredientDisplays.size(); i++) {
            List<ItemStack> stacks = ingredientDisplays.get(i)
                    .resolveForStacks(new ContextMap.Builder()
                            .withOptionalParameter(SlotDisplayContext.REGISTRIES, Minecrft.registryAccess())
                            .create(SlotDisplayContext.CONTEXT));

            if (stacks.isEmpty())
                tooltipComponents.accept(Component.literal("  ").append(Component.literal("?").withStyle(yellow)));
            else if (stacks.size() == 1) {
                tooltipComponents.accept(Component.literal("  ").append(stacks.getFirst().getHoverName().copy().withStyle(yellow)));
            } else { // Cycle stacks if it's not one:
                int val = (int) Math.ceil((Minecraft.getInstance().level.getGameTime() + 10 * i) % (20f * stacks.size()) / 20f);
                int index = Mth.clamp(val - 1, 0, stacks.size() - 1);

                tooltipComponents.accept(Component.literal("  ").append(stacks.get(index).getHoverName().copy().withStyle(yellow)));
            }
        }
    }
}
