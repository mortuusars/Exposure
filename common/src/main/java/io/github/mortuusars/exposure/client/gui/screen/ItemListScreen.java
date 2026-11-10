package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.mortaar.util.Easing;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemListScreen extends Screen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/item_list.png");

    protected final Screen parent;
    protected final List<ItemStack> items;
    protected final int rowsCount;
    protected final Animation openingAnimation;

    protected int imageWidth = 176;
    protected int imageHeight = 166;
    protected int titleLabelX = 8;
    protected int titleLabelY = 6;
    protected int leftPos;
    protected int topPos;

    @Nullable
    protected Slot hoveredSlot;
    protected List<Slot> slots = new ArrayList<>();

    protected long openedAt;

    public ItemListScreen(Screen parent, Component title, List<ItemStack> items) {
        super(title);
        this.parent = parent;
        this.items = items;
        List<List<ItemStack>> rows = Lists.partition(items, 9);
        this.rowsCount = rows.size();
        this.openingAnimation = new Animation(200, Easing.OUT_EXPO);

        this.openedAt = Util.getMillis();

        SimpleContainer container = new SimpleContainer(items.toArray(ItemStack[]::new));

        int rowX = 8;
        int rowY = 18;

        for (int row = 0; row < rows.size(); row++) {
            List<ItemStack> stacks = rows.get(row);

            // Centers row if it has fewer items than 9
            int rowXToCenterOffset = ((9 * 18) - (stacks.size() * 18)) / 2;

            for (int column = 0; column < stacks.size(); column++) {
                int slotIndex = row * 9 + column;
                slots.add(new Slot(container, slotIndex, rowX + rowXToCenterOffset + (column * 18), rowY) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }
                });
            }

            rowY += 18;
        }

        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 1f));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.imageWidth = 176;
        this.imageHeight = 24 + (rowsCount * 18);
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = leftPos;
        int top = topPos;

        renderTransparentBackground(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((width / 2f), (height / 2f), 0.0f);
        float animProgress = (float)openingAnimation.getValue();
        guiGraphics.pose().scale(animProgress, animProgress, animProgress);
        guiGraphics.pose().translate(-(width / 2f), -(height / 2f), 0.0f);

        renderBg(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.disableDepthTest();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(left, top, 0.0f);
            hoveredSlot = null;
            for (Slot slot : slots) {
                if (slot.isActive()) {
                    renderSlot(guiGraphics, slot);
                }
                if (!isHovering(slot, mouseX, mouseY) || !slot.isActive()) {
                    continue;
                }
                this.hoveredSlot = slot;
                if (!hoveredSlot.isHighlightable()) {
                    continue;
                }
                renderSlotHighlight(guiGraphics, slot.x, slot.y, 0);
            }
            this.renderLabels(guiGraphics, mouseX, mouseY);
            guiGraphics.pose().popPose();
        }
        RenderSystem.enableDepthTest();
        guiGraphics.pose().popPose();

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    protected void renderBg(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Render BG expanding it according to number of rows
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, 17);
        for (int i = 0; i < rowsCount; i++) {
            guiGraphics.blit(TEXTURE, leftPos, topPos + 17 + (i * 18), 0, 17, imageWidth, 18);
        }
        guiGraphics.blit(TEXTURE, leftPos, topPos + 17 + (rowsCount * 18), 0, 35, imageWidth, 7);

        for (Slot slot : slots) {
            guiGraphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 176, 0, 18, 18);
        }
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }

    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        int x = slot.x;
        int y = slot.y;
        ItemStack itemStack = slot.getItem();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 100.0f);
        guiGraphics.renderItem(itemStack, x, y, slot.x + slot.y * imageWidth);
        guiGraphics.renderItemDecorations(font, itemStack, x, y, null);
        guiGraphics.pose().popPose();
    }

    public static void renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int blitOffset) {
        guiGraphics.fillGradient(RenderType.guiOverlay(), x, y, x + 16, y + 16, -2130706433, -2130706433, blitOffset);
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack itemStack = hoveredSlot.getItem();

            List<Component> tooltipLines = parent instanceof AbstractContainerScreen<?> abstractContainerScreen
                    ? abstractContainerScreen.getTooltipFromContainerItem(itemStack)
                    : Screen.getTooltipFromItem(Minecrft.get(), itemStack);

            guiGraphics.renderTooltip(font, tooltipLines, itemStack.getTooltipImage(), x, y);
        }
    }

    protected boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
    }

    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        return (mouseX -= i) >= (double) (x - 1) && mouseX < (double) (x + width + 1) && (mouseY -= j) >= (double) (y - 1) && mouseY < (double) (y + height + 1);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Minecrft.options().keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (!isHovering(0, 0, imageWidth, imageHeight, mouseX, mouseY)) {
            onClose();
            return true;
        }

        return false;
    }

    @Override
    public void onClose() {
        Minecrft.get().setScreen(parent);
    }
}
