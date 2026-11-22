package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.TextBox;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class AlbumSigningScreen extends Screen {
    public static final WidgetSprites CANCEL_BUTTON_SPRITE = new WidgetSprites(
            Exposure.resource("album/cancel"), Exposure.resource("album/cancel_disabled"), Exposure.resource("album/cancel_highlighted"));

    public static final int SELECTION_COLOR = 0xFF8888FF;
    public static final int SELECTION_UNFOCUSED_COLOR = 0xFFBBBBFF;

    protected final AlbumScreen parentScreen;

    protected int imageWidth, imageHeight, leftPos, topPos;

    protected TextBox titleTextBox;
    protected ImageButton signButton;
    protected ImageButton cancelSigningButton;

    protected String titleText = "";

    public AlbumSigningScreen(AlbumScreen parent) {
        super(Component.empty());
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        this.imageWidth = 149;
        this.imageHeight = 188;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // TITLE
        titleTextBox = new TextBox(font, leftPos + 21, topPos + 73, 108, 9,
                () -> titleText, text -> titleText = text)
                .setFontColor(Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR))
                .setSelectionColor(SELECTION_COLOR, SELECTION_UNFOCUSED_COLOR);
        titleTextBox.textValidator = text -> text != null && font.wordWrapHeight(Component.literal(text), 108) <= 9 && !text.contains("\n");
        titleTextBox.horizontalAlignment = HorizontalAlignment.CENTER;
        addRenderableWidget(titleTextBox);

        // SIGN
        signButton = new ImageButton(leftPos + 46, topPos + 110, 22, 22,
                AlbumScreen.SIGN_BUTTON_SPRITES, b -> signAlbum(), Component.translatable("gui.exposure.album.sign"));
        MutableComponent component = Component.translatable("gui.exposure.album.sign")
                .append("\n").append(Component.translatable("gui.exposure.album.sign.warning").withStyle(ChatFormatting.GRAY));
        signButton.setTooltip(Tooltip.create(component));
        addRenderableWidget(signButton);

        // CANCEL
        cancelSigningButton = new ImageButton(leftPos + 83, topPos + 111, 22, 22,
                CANCEL_BUTTON_SPRITE, b -> cancelSigning(), Component.translatable("gui.exposure.album.cancel_signing"));
        cancelSigningButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.album.cancel_signing")));
        addRenderableWidget(cancelSigningButton);

        setInitialFocus(titleTextBox);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        titleTextBox.tick();
    }

    private void updateButtons() {
        signButton.active = canSign();
    }

    protected boolean canSign() {
        return !titleText.isEmpty();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateButtons();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderLabels(guiGraphics);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(guiGraphics);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AlbumGUI.TEXTURE, leftPos, topPos, 298,
                0, imageWidth, imageHeight, 512, 512);
    }

    private void renderLabels(GuiGraphics guiGraphics) {
        MutableComponent component = Component.translatable("gui.exposure.album.enter_title");
        guiGraphics.drawString(font, component,  leftPos + 149 / 2 - font.width(component) / 2, topPos + 50, 0xf5ebd0, false);

        component = Component.translatable("gui.exposure.album.by_author", Minecrft.player().getScoreboardName());
        guiGraphics.drawString(font, component, leftPos + 149 / 2 - font.width(component) / 2, topPos + 84, 0xc7b496, false);
    }

    protected void signAlbum() {
        if (canSign()) {
            parentScreen.getMenu().setTitle(titleText);
            parentScreen.getMenu().signAlbum(Minecrft.player());
            this.onClose();
        }
    }

    protected void cancelSigning() {
        Minecrft.get().setScreen(parentScreen);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_TAB) {
            return super.keyPressed(event);
        }

        if (event.key() == InputConstants.KEY_ESCAPE) {
            cancelSigning();
            return true;
        }

        if (titleTextBox.isFocused()) {
            return titleTextBox.keyPressed(event);
        }

        return super.keyPressed(event);
    }
}
