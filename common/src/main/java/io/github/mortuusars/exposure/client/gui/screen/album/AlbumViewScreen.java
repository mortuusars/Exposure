package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.gui.screen.element.TextBlock;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.util.Side;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.component.album.AlbumContent;
import io.github.mortuusars.exposure.world.item.component.album.AlbumPage;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumContent;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumPage;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class AlbumViewScreen extends Screen {
    protected final Pager pager = new Pager()
            .setChangeSound(new SoundEffect(() -> SoundEvents.BOOK_PAGE_TURN))
            .onPageChanged(this::onSpreadChanged);

    protected final KeyBindings keyBindings = KeyBindings.of(
            Key.press(Minecrft.options().keyInventory).executes(this::onClose),
            Key.press(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::previousPage),
            Key.press(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::nextPage),
            Key.release(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::resetCooldown),
            Key.release(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::resetCooldown)
    );

    protected final List<Page> pages = new ArrayList<>();

    protected AlbumAccess albumAccess;
    protected int imageWidth;
    protected int imageHeight;
    protected int leftPos;
    protected int topPos;

    public AlbumViewScreen(AlbumAccess albumAccess) {
        super(Component.empty());
        this.albumAccess = albumAccess;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.imageWidth = 298;
        this.imageHeight = 188;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        pages.clear();

        // LEFT:
        Page leftPage = createPage(Side.LEFT, 0);
        pages.add(leftPage);

        ImageButton previousPageButton = new ImageButton(leftPos + 12, topPos + 164, 13, 15,
                AlbumGUI.PREVIOUS_PAGE_BUTTON_SPRITES, button -> pager.changePage(PagingDirection.PREVIOUS), Component.translatable("gui.exposure.previous_page"));
        previousPageButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.previous_page")));
        addRenderableWidget(previousPageButton);

        // RIGHT:
        Page rightPage = createPage(Side.RIGHT, 140);
        pages.add(rightPage);

        ImageButton nextPageButton = new ImageButton(leftPos + 273, topPos + 164, 13, 15,
                AlbumGUI.NEXT_PAGE_BUTTON_SPRITES, button -> pager.changePage(PagingDirection.NEXT), Component.translatable("gui.exposure.next_page"));
        nextPageButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.next_page")));
        addRenderableWidget(nextPageButton);

        int spreadsCount = (int) Math.ceil(albumAccess.pages().size() / 2f);
        pager.setPagesCount(spreadsCount)
                .setPreviousPageButton(previousPageButton)
                .setNextPageButton(nextPageButton);
    }

    protected Page createPage(Side side, int xOffset) {
        int x = leftPos + xOffset;
        int y = topPos;

        PhotographSlotWidget photographWidget = new PhotographSlotWidget(this, x + 25, y + 21, 108, 108,
                () -> getPage(side).orElse(PageContent.EMPTY).photograph())
                .editable(false)
                .primaryAction(widget -> inspectPhotograph(widget.getPhotograph()));

        addRenderableWidget(photographWidget);

        TextBlock noteWidget = new TextBlock(font, x + 22, y + 133, 114, 27,
                getPage(side).orElse(PageContent.EMPTY).note(), this::handleComponentClicked);
        noteWidget.fontColor = Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR);
        noteWidget.alignment = HorizontalAlignment.CENTER;
        noteWidget.drawShadow = false;
        //  TextBlock is rendered manually to not be a part of TAB navigation.
        //  addRenderableWidget(noteWidget);

        return new Page(side, photographWidget, noteWidget);
    }

    public List<PageContent> getPages() {
        return albumAccess.pages();
    }

    public Optional<PageContent> getPage(int pageIndex) {
        if (pageIndex <= getPages().size() - 1)
            return Optional.ofNullable(getPages().get(pageIndex));

        return Optional.empty();
    }

    public Optional<PageContent> getPage(Side side) {
        return getPage(getCurrentSpreadIndex() * 2 + side.getIndex());
    }

    public int getCurrentSpreadIndex() {
        return pager.getPage();
    }

    protected void onSpreadChanged(int oldSpread, int newSpread) {
        forEachPage(page -> {
            Component note = getPage(page.side).orElse(PageContent.EMPTY).note();
            page.noteWidget().setMessage(note);
        });
    }

    public void setAlbumAccess(AlbumAccess albumAccess) {
        this.albumAccess = albumAccess;
        pager.setPagesCount(albumAccess.getPageCount() / 2);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        for (Page page : pages) {
            AbstractWidget noteWidget = page.noteWidget();
            if (noteWidget instanceof TextBlock textBlock) {
                textBlock.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        for (Page page : pages) {
            if (page.photographWidget().isHoveredOrFocused()) {
                page.photographWidget().renderTooltip(guiGraphics, x, y);
                return;
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(guiGraphics);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(AlbumGUI.TEXTURE, leftPos, topPos, 0, 0, 0,
                imageWidth, imageHeight, 512, 512);

        int currentSpreadIndex = getCurrentSpreadIndex();
        drawPageNumbers(guiGraphics, currentSpreadIndex, mouseX, mouseY);
    }

    protected void drawPageNumbers(GuiGraphics guiGraphics, int currentSpreadIndex, int mouseX, int mouseY) {
        Font font = Minecrft.get().font;

        String leftPageNumber = Integer.toString(currentSpreadIndex * 2 + 1);
        String rightPageNumber = Integer.toString(currentSpreadIndex * 2 + 2);

        guiGraphics.drawString(font, leftPageNumber, leftPos + 71 + (8 - font.width(leftPageNumber) / 2),
                topPos + 167, Config.getColor(Config.Client.ALBUM_FONT_SECONDARY_COLOR), false);

        guiGraphics.drawString(font, rightPageNumber, leftPos + 212 + (8 - font.width(rightPageNumber) / 2),
                topPos + 167, Config.getColor(Config.Client.ALBUM_FONT_SECONDARY_COLOR), false);
    }

    // --

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        for (Page page : pages) {
            if (page.noteWidget().mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean handleComponentClicked(@Nullable Style style) {
        if (style == null)
            return false;

        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null)
            return false;
        else if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String pageIndexStr = clickEvent.getValue();
            int pageIndex = Integer.parseInt(pageIndexStr) - 1;
            forcePage(pageIndex);
            return true;
        }

        boolean handled = super.handleComponentClicked(style);
        if (handled && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND)
            onClose();
        return handled;
    }

    protected void forcePage(int pageIndex) {
        pager.changePage(pageIndex / 2);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyBindings.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return keyBindings.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    // --

    protected void inspectPhotograph(ItemStack photograph) {
        if (!(photograph.getItem() instanceof PhotographItem)) {
            return;
        }

        Minecrft.get().setScreen(new ChildPhotographScreen(this, List.of(new ItemAndStack<>(photograph))));
        Minecrft.get().getSoundManager()
                .play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(),
                        Minecrft.levelOrThrow().getRandom().nextFloat() * 0.2f + 1.3f, 0.75f));
    }

    protected void forEachPage(Consumer<Page> pageAction) {
        for (Page page : pages) {
            pageAction.accept(page);
        }
    }

    protected record Page(Side side, PhotographSlotWidget photographWidget, TextBlock noteWidget) { }

    public record AlbumAccess(List<PageContent> pages) {
        public static final AlbumAccess EMPTY = new AlbumAccess(Collections.emptyList());

        public int getPageCount() {
            return this.pages.size();
        }

        public static AlbumAccess fromItem(ItemStack stack) {
            if (stack.get(Exposure.DataComponents.ALBUM_CONTENT) instanceof AlbumContent content) {
                return new AlbumAccess(content.pages().stream().map(PageContent::new).toList());
            }

            if (stack.get(Exposure.DataComponents.SIGNED_ALBUM_CONTENT) instanceof SignedAlbumContent content) {
                return new AlbumAccess(content.pages().stream().map(PageContent::new).toList());
            }

            return EMPTY;
        }
    }

    public record PageContent(ItemStack photograph, Component note) {
        public static final PageContent EMPTY = new PageContent(ItemStack.EMPTY, Component.empty());

        public PageContent(AlbumPage page) {
            this(page.photograph(), Component.literal(page.note()));
        }

        public PageContent(SignedAlbumPage page) {
            this(page.photograph(), page.note());
        }
    }
}
