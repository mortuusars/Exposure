package io.github.mortuusars.exposure.integration.jei;

import com.google.common.collect.ImmutableList;
import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.integration.jei.category.PhotographPrintingCategory;
import io.github.mortuusars.exposure.integration.jei.category.PhotographStackingCategory;
import io.github.mortuusars.exposure.integration.jei.recipe.ComponentTransferringShapelessExtension;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographPrintingJeiRecipe;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographStackingJeiRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import io.github.mortuusars.mortaar.Platform;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class ExposureJeiPlugin implements IModPlugin {
    public static final RecipeType<PhotographPrintingJeiRecipe> PHOTOGRAPH_PRINTING_RECIPE_TYPE =
            RecipeType.create(Exposure.ID, "photograph_printing", PhotographPrintingJeiRecipe.class);
    public static final RecipeType<PhotographStackingJeiRecipe> PHOTOGRAPH_STACKING_RECIPE_TYPE =
            RecipeType.create(Exposure.ID, "photograph_stacking", PhotographStackingJeiRecipe.class);

    private static final ResourceLocation ID = Exposure.resource("jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PhotographPrintingCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new PhotographStackingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Exposure.Items.LIGHTROOM.get()), PHOTOGRAPH_PRINTING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Exposure.Items.STACKED_PHOTOGRAPHS.get()), PHOTOGRAPH_STACKING_RECIPE_TYPE);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        registration.addRecipes(PHOTOGRAPH_PRINTING_RECIPE_TYPE, ImmutableList.of(
                new PhotographPrintingJeiRecipe(ExposureType.BLACK_AND_WHITE),
                new PhotographPrintingJeiRecipe(ExposureType.COLOR)
        ));

        registration.addRecipes(PHOTOGRAPH_STACKING_RECIPE_TYPE, ImmutableList.of(
                new PhotographStackingJeiRecipe(PhotographStackingJeiRecipe.STACKING),
                new PhotographStackingJeiRecipe(PhotographStackingJeiRecipe.REMOVING)
        ));

        if (Config.Client.SHOW_JEI_INFORMATION.get()) {
            registration.addItemStackInfo(List.of(new ItemStack(Exposure.Items.PHOTOGRAPH_FRAME.get()),
                            new ItemStack(Exposure.Items.CLEAR_PHOTOGRAPH_FRAME.get())),
                    Component.translatable("exposure.jei.info.photograph_frame"));
        }

        if (Platform.isModLoaded("create")) {
            addSequencedDevelopingRecipes(registration);
        }
    }

    @ExpectPlatform
    public static void addSequencedDevelopingRecipes(@NotNull IRecipeRegistration registration) {
        throw new AssertionError();
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addExtension(ComponentTransferringRecipe.class, new ComponentTransferringShapelessExtension());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AlbumScreen.class, new IGuiContainerHandler<AlbumScreen>() {
            @Override
            public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull AlbumScreen containerScreen) {
                return List.of(new Rect2i(0, 0,
                        Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                        Minecraft.getInstance().getWindow().getGuiScaledHeight()));
            }
        });

        registration.addGenericGuiContainerHandler(ItemRenameScreen.class, new IGuiContainerHandler<ItemRenameScreen>() {
            @Override
            public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull ItemRenameScreen containerScreen) {
                return List.of(new Rect2i(0, 0,
                        Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                        Minecraft.getInstance().getWindow().getGuiScaledHeight()));
            }
        });
    }
}