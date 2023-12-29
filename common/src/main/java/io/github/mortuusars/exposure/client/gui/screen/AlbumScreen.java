package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.menu.AlbumMenu;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.AlbumMovePhotoC2SP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class AlbumScreen extends AbstractContainerScreen<AlbumMenu> {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/album.png");
    public static final int MAIN_FONT_COLOR = 0xB59774;
    public static final int SECONDARY_FONT_COLOR = 0xEFE4CA;

    private final Pager pager = new Pager(TEXTURE) {
        @Override
        public void init(int screenWidth, int screenHeight, int pages, boolean cycled, Consumer<AbstractButton> addButtonAction) {
            this.pages = pages;
            this.cycled = cycled;
            previousButton = new ImageButton(leftPos + 12, topPos + 164, 13, 15,
                    407, 0, 15, texture, 512, 512, button -> onPreviousButtonPressed());
            nextButton = new ImageButton(leftPos + 275, topPos + 164, 13, 15,
                    420, 0, 15, texture, 512, 512, button -> onNextButtonPressed());

            previousButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.album.previous_page")));
            nextButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.album.next_page")));

            addButtonAction.accept(previousButton);
            addButtonAction.accept(nextButton);

            update();
        }

        @Override
        protected SoundEvent getChangeSound() {
            return SoundEvents.BOOK_PAGE_TURN;
        }
    };

    public AlbumScreen(AlbumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void added() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 1f));
    }

    @Override
    protected void init() {
        this.imageWidth = 299;
        this.imageHeight = 188;
        super.init();

        List<AlbumItem.Page> pages = getMenu().getPages();

        pager.init(width, height, (int)Math.ceil(pages.size() / 2f), false, this::addRenderableWidget);

        titleLabelY = -999;
        inventoryLabelY = -999;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        pager.update();
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 0,
                this.imageWidth, this.imageHeight, 512, 512);

        int currentSpreadIndex = pager.getCurrentPageIndex();

        drawPageNumbers(guiGraphics, currentSpreadIndex);

        List<AlbumItem.Page> pages = getMenu().getPages();

        int leftPage = currentSpreadIndex * 2;
        int rightPage = leftPage + 1;

        if (leftPage < pages.size()) {
            AlbumItem.Page page = pages.get(leftPage);
            ItemStack photoStack = page.getPhotographStack();

            if (photoStack.getItem() instanceof PhotographItem photographItem) {
                guiGraphics.blit(TEXTURE, leftPos + 25, topPos + 21, 0, 299, 0,
                        108, 109, 512, 512);

                @Nullable Either<String, ResourceLocation> idOrTexture = photographItem.getIdOrTexture(photoStack);
                if (idOrTexture != null) {
                    MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                    ExposureClient.getExposureRenderer().render(idOrTexture, false, false, guiGraphics.pose(),
                            bufferSource, this.leftPos + 31, this.topPos + 27, this.leftPos + 127, this.topPos + 123,
                            0, 0, 1, 1, LightTexture.FULL_BRIGHT, 255, 255, 255, 255);
                    bufferSource.endBatch();
                }
            }
        }

        if (rightPage < pages.size()) {
            AlbumItem.Page page = pages.get(rightPage);
            ItemStack photoStack = page.getPhotographStack();

            if (photoStack.getItem() instanceof PhotographItem photographItem) {
                guiGraphics.blit(TEXTURE, leftPos + 166, topPos + 21, 0, 299, 0,
                        108, 109, 512, 512);

                @Nullable Either<String, ResourceLocation> idOrTexture = photographItem.getIdOrTexture(photoStack);
                if (idOrTexture != null) {
                    MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                    ExposureClient.getExposureRenderer().render(idOrTexture, false, false, guiGraphics.pose(),
                            bufferSource, this.leftPos + 172, this.topPos + 27, this.leftPos + 268, this.topPos + 123,
                            0, 0, 1, 1, LightTexture.FULL_BRIGHT, 255, 255, 255, 255);
                    bufferSource.endBatch();
                }
            }
        }
    }

    protected void drawPageNumbers(GuiGraphics guiGraphics, int currentSpreadIndex) {
        Font font = Minecraft.getInstance().font;

        String leftPageNumber = Integer.toString(currentSpreadIndex * 2 + 1);
        String rightPageNumber = Integer.toString(currentSpreadIndex * 2 + 2);

        guiGraphics.drawString(font, leftPageNumber, leftPos + 71 + (8 - font.width(leftPageNumber) / 2),
                topPos + 167, SECONDARY_FONT_COLOR, false);

        guiGraphics.drawString(font, rightPageNumber, leftPos + 212 + (8 - font.width(rightPageNumber) / 2),
                topPos + 167, SECONDARY_FONT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= 25 && mouseX < 132 && mouseY >= 21 && mouseY < 128) {
            int currentSpreadIndex = pager.getCurrentPageIndex();
            List<AlbumItem.Page> pages = getMenu().getPages();

            int leftPage = currentSpreadIndex * 2;
            if (leftPage < pages.size()) {
                ItemStack photographStack = pages.get(leftPage).getPhotographStack();
                if (photographStack.isEmpty()) {
                    for (int i = 0; i < 40; i++) {
                        ItemStack stack = Minecraft.getInstance().player.getInventory().getItem(i);
                        if (stack.getItem() instanceof PhotographItem) {
                            Minecraft.getInstance().player.getInventory().setItem(i, ItemStack.EMPTY);
                            pages.get(leftPage).setPhotographStack(stack);
                            Packets.sendToServer(new AlbumMovePhotoC2SP(i, leftPage, false));
                            break;
                        }
                    }
                }
                else {
                    ItemStack removedStack = pages.get(leftPage).setPhotographStack(ItemStack.EMPTY);
                    boolean added = Minecraft.getInstance().player.getInventory().add(removedStack);
                    if (!added) {
                        Minecraft.getInstance().player.drop(removedStack, true, false);
                    }
                    pages.get(leftPage).setPhotographStack(ItemStack.EMPTY);
                    Packets.sendToServer(new AlbumMovePhotoC2SP(-1, leftPage, true));
                }

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return pager.handleKeyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return pager.handleKeyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }
}
