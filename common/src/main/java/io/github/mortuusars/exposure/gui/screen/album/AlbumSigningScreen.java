package io.github.mortuusars.exposure.gui.screen.album;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.gui.screen.element.textbox.HorizontalAlignment;
import io.github.mortuusars.exposure.gui.screen.element.textbox.TextBox;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.AlbumSignC2SP;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AlbumSigningScreen extends Screen {
    public static final int SELECTION_COLOR = 0xFF8888FF;
    public static final int SELECTION_UNFOCUSED_COLOR = 0xFFBBBBFF;

    @NotNull
    protected final Minecraft minecraft;
    @NotNull
    protected final Player player;

    protected final Screen parentScreen;
    protected final ResourceLocation texture;

    protected int imageWidth, imageHeight, leftPos, topPos, textureWidth, textureHeight;

    protected TextBox titleTextBox;
    protected ImageButton signButton;
    protected ImageButton cancelSigningButton;

    protected String titleText = "";

    public AlbumSigningScreen(Screen screen, ResourceLocation texture, int textureWidth, int textureHeight) {
        super(Component.empty());
        this.parentScreen = screen;
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;

        minecraft = Minecraft.getInstance();
        player = Objects.requireNonNull(minecraft.player);
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
                .setFontColor(0xFF856036, 0xFF856036)
                .setSelectionColor(SELECTION_COLOR, SELECTION_UNFOCUSED_COLOR);
        titleTextBox.textValidator = text -> text != null && font.wordWrapHeight(text, 108) <= 9 && !text.contains("\n");
        titleTextBox.horizontalAlignment = HorizontalAlignment.CENTER;
        addRenderableWidget(titleTextBox);

        // SIGN
        signButton = new ImageButton(leftPos + 46, topPos + 110, 22, 22, 242, 188,
                22, texture, textureWidth, textureHeight,
                b -> signAlbum(),
            (b, poseStack, x, y) -> {
                renderTooltip(poseStack, List.of(Component.translatable("gui.exposure.album.sign"),
                        Component.translatable("gui.exposure.album.sign.warning").withStyle(ChatFormatting.GRAY)),
                        Optional.empty(), x, y);
            }, Component.translatable("gui.exposure.album.sign"));
        addRenderableWidget(signButton);

        // CANCEL
        cancelSigningButton = new ImageButton(leftPos + 83, topPos + 111, 22, 22, 264, 188,
                22, texture, textureWidth, textureHeight,
                b -> cancelSigning(),
                (b, poseStack, x, y) -> {
                    renderTooltip(poseStack, Component.translatable("gui.exposure.album.cancel_signing"), x, y);
                }, Component.translatable("gui.exposure.album.cancel_signing"));
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

    protected void updateButtons() {
        signButton.active = canSign();
    }

    protected boolean canSign() {
        return !titleText.isEmpty();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        updateButtons();

        RenderSystem.setShaderTexture(0, texture);

        renderBackground(poseStack);
        blit(poseStack, leftPos, topPos, 0, 298,
                0, imageWidth, imageHeight, textureHeight, textureWidth);
        super.render(poseStack, mouseX, mouseY, partialTick);

        renderLabels(poseStack);
    }

    private void renderLabels(@NotNull PoseStack poseStack) {
        MutableComponent component = Component.translatable("gui.exposure.album.enter_title");
        font.draw(poseStack, component,  (int)(leftPos + 149 / 2 - font.width(component) / 2), topPos + 50, 0xf5ebd0);

        component = Component.translatable("gui.exposure.album.by_author", player.getName());
        font.draw(poseStack, component, (int)(leftPos + 149 / 2 - font.width(component) / 2), topPos + 84, 0xc7b496);
    }

    protected void signAlbum() {
        if (canSign()) {
            Packets.sendToServer(new AlbumSignC2SP(titleText));
            this.onClose();
        }
    }

    protected void cancelSigning() {
        minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_TAB)
            return super.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == InputConstants.KEY_ESCAPE) {
            cancelSigning();
            return true;
        }

        if (titleTextBox.isFocused())
            return titleTextBox.keyPressed(keyCode, scanCode, modifiers);

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
