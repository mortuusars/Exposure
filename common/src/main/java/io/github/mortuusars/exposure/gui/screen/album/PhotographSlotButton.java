package io.github.mortuusars.exposure.gui.screen.album;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.render.modifiers.ExposurePixelModifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class PhotographSlotButton extends ImageButton {

    protected final Rect2i exposureArea;
    protected final OnPress onRightButtonPress;
    protected final Supplier<ItemStack> photograph;
    protected final boolean isEditable;
    protected boolean hasPhotograph;

    public PhotographSlotButton(Rect2i exposureArea, int x, int y, int width, int height, int xTexStart, int yTexStart,
                                int yDiffTex, ResourceLocation resourceLocation, int textureWidth, int textureHeight,
                                OnPress onLeftButtonPress, OnPress onRightButtonPress, Supplier<ItemStack> photographGetter, boolean isEditable) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight, onLeftButtonPress,
                Component.translatable("item.exposure.photograph"));
        this.exposureArea = exposureArea;
        this.onRightButtonPress = onRightButtonPress;
        this.photograph = photographGetter;
        this.isEditable = isEditable;
    }

    public ItemStack getPhotograph() {
        return photograph.get();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ItemStack photograph = getPhotograph();
        hasPhotograph = photograph.getItem() instanceof PhotographItem;

        int xTex = xTexStart + (hasPhotograph ? getWidth() : 0);
        renderTexture(guiGraphics, resourceLocation, getX(), getY(), xTex, yTexStart, yDiffTex, width, height, textureWidth, textureHeight);

        if (photograph.getItem() instanceof PhotographItem photographItem) {
            @Nullable Either<String, ResourceLocation> idOrTexture = photographItem.getIdOrTexture(photograph);
            if (idOrTexture != null) {
                ExposureClient.getExposureRenderer().render(idOrTexture, ExposurePixelModifiers.EMPTY, guiGraphics.pose(),
                        exposureArea.getX(), exposureArea.getY(), exposureArea.getWidth(), exposureArea.getHeight());
            }
        }
    }

    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isEditable && !hasPhotograph) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font,
                    Component.translatable("gui.exposure.album.add_photograph"), mouseX, mouseY);
            return;
        }

        ItemStack photograph = getPhotograph();
        if (photograph.isEmpty())
            return;

        List<Component> itemTooltip = Screen.getTooltipFromItem(Minecraft.getInstance(), photograph);
        itemTooltip.add(Component.translatable("gui.exposure.album.left_click_or_scroll_up_to_view"));
        if (isEditable)
            itemTooltip.add(Component.translatable("gui.exposure.album.right_click_to_remove"));

        // Photograph image in tooltip is not rendered here

        if (isFocused()) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, Lists.transform(itemTooltip,
                    Component::getVisualOrderText), createTooltipPositioner(), mouseX, mouseY);
        }
        else
            guiGraphics.renderTooltip(Minecraft.getInstance().font, itemTooltip, Optional.empty(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || !clicked(mouseX, mouseY))
            return false;

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            this.onPress.onPress(this);
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            this.onRightButtonPress.onPress(this);
        } else
            return false;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && clicked(mouseX, mouseY) && hasPhotograph) {
            this.onPress.onPress(this);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible && Screen.hasShiftDown() && CommonInputs.selected(keyCode)) {
            onRightButtonPress.onPress(this);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
