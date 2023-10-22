package io.github.mortuusars.exposure.integration.jei.category;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.integration.jei.ExposureJeiPlugin;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographStackingJeiRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhotographStackingCategory implements IRecipeCategory<PhotographStackingJeiRecipe> {
    private static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/jei/photograph_stacking.png");
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic stackCursor;
    private final IDrawableStatic removeCursor;

    public PhotographStackingCategory(IGuiHelper guiHelper) {
        title = Component.translatable("jei.exposure.photograph_stacking.title");
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 109, 38);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Exposure.Items.STACKED_PHOTOGRAPHS.get()));

        stackCursor = guiHelper.createDrawable(TEXTURE, 109, 0, 20, 20);
        removeCursor = guiHelper.createDrawable(TEXTURE, 109, 38, 20, 20);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(PhotographStackingJeiRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (recipe.getType() == PhotographStackingJeiRecipe.STACKING && mouseX >= 10 && mouseX < 38 && mouseY >= 4 && mouseY < 28)
            return List.of(Component.translatable("jei.exposure.photograph_stacking.stacking.tooltip"));

        if (recipe.getType() == PhotographStackingJeiRecipe.REMOVING && mouseX >= 10 && mouseX < 37 && mouseY >= 13 && mouseY < 35)
            return List.of(Component.translatable("jei.exposure.photograph_stacking.removing.tooltip"));

        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull PhotographStackingJeiRecipe recipe, @NotNull IFocusGroup focuses) {
        if (recipe.getType() == PhotographStackingJeiRecipe.STACKING) {
            builder.addSlot(RecipeIngredientRole.INPUT, 11, 11)
                    .addItemStacks(List.of(new ItemStack(Exposure.Items.PHOTOGRAPH.get()), new ItemStack(Exposure.Items.STACKED_PHOTOGRAPHS.get())))
                    .setOverlay(stackCursor, 7, -6)
                    .setSlotName("Input");

            builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 11)
                    .addItemStack(new ItemStack(Exposure.Items.STACKED_PHOTOGRAPHS.get()))
                    .setSlotName("Result");
        }

        if (recipe.getType() == PhotographStackingJeiRecipe.REMOVING) {
            builder.addSlot(RecipeIngredientRole.INPUT, 11, 11)
                    .addItemStack(new ItemStack(Exposure.Items.STACKED_PHOTOGRAPHS.get()))
                    .setOverlay(removeCursor, 11, 4)
                    .setSlotName("Input");

            builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 11)
                    .addItemStack(new ItemStack(Exposure.Items.PHOTOGRAPH.get()))
                    .setSlotName("Result");
        }
    }

    @Override
    public @NotNull RecipeType<PhotographStackingJeiRecipe> getRecipeType() {
        return ExposureJeiPlugin.PHOTOGRAPH_STACKING_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }
}