package io.github.mortuusars.exposure.gui.screen.element.textbox;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.util.Pos2i;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TextBox extends AbstractWidget {
    public final Font font;
    public Supplier<String> textGetter;
    public Consumer<String> textSetter;
    public Predicate<String> textValidator = text -> text != null
            && getFont().wordWrapHeight(text, width) + (text.endsWith("\n") ? getFont().lineHeight : 0) <= height;

    public HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    public int fontColor = 0xFF000000;
    public int fontUnfocusedColor = 0xFF000000;
    public int selectionColor = 0xFF0000FF;
    public int selectionUnfocusedColor = 0x880000FF;

    public final TextFieldHelper textFieldHelper;
    protected DisplayCache displayCache = new DisplayCache();
    protected int frameTick;
    protected long lastClickTime;
    protected int lastIndex = -1;

    public TextBox(@NotNull Font font, int x, int y, int width, int height,
                   Supplier<String> textGetter, Consumer<String> textSetter) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.textGetter = textGetter;
        this.textSetter = textSetter;
        textFieldHelper = new TextFieldHelper(this::getText, this::setText,
                TextFieldHelper.createClipboardGetter(Minecraft.getInstance()),
                TextFieldHelper.createClipboardSetter(Minecraft.getInstance()),
                this::validateText);
    }

    public void tick() {
        ++frameTick;
    }

    public Font getFont() {
        return font;
    }

    public @NotNull String getText() {
        return textGetter.get();
    }

    public TextBox setText(@NotNull String text) {
        textSetter.accept(text);
        clearDisplayCache();
        return this;
    }

    protected boolean validateText(String text) {
        return textValidator.test(text);
    }

    public void setHeight(int height) {
        this.height = height;
        clearDisplayCache();
    }

    public int getCurrentFontColor() {
        return isFocused() ? fontColor : fontUnfocusedColor;
    }

    public TextBox setFontColor(int fontColor, int fontUnfocusedColor) {
        this.fontColor = fontColor;
        this.fontUnfocusedColor = fontUnfocusedColor;
        clearDisplayCache();
        return this;
    }

    public TextBox setSelectionColor(int selectionColor, int selectionUnfocusedColor) {
        this.selectionColor = selectionColor;
        this.selectionUnfocusedColor = selectionUnfocusedColor;
        clearDisplayCache();
        return this;
    }

    public void setCursorToEnd() {
        textFieldHelper.setCursorToEnd();
        clearDisplayCache();
    }

    public void refresh() {
        clearDisplayCache();
    }

    protected DisplayCache getDisplayCache() {
        if (displayCache.needsRebuilding)
            displayCache.rebuild(font, getText(), textFieldHelper.getCursorPos(), textFieldHelper.getSelectionPos(),
                    x, this.y, getWidth(), getHeight(), horizontalAlignment);
        return displayCache;
    }

    protected void clearDisplayCache() {
        displayCache.needsRebuilding = true;
    }

    protected Pos2i convertLocalToScreen(Pos2i pos) {
        return new Pos2i(x + pos.x, this.y + pos.y);
    }

    protected Pos2i convertScreenToLocal(Pos2i screenPos) {
        return new Pos2i(screenPos.x - x, screenPos.y - this.y);
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        DisplayCache displayCache = this.getDisplayCache();
        for (DisplayCache.LineInfo lineInfo : displayCache.lines) {
            font.draw(poseStack, lineInfo.asComponent, x + lineInfo.x, this.y + lineInfo.y, getCurrentFontColor());
        }
        this.renderHighlight(displayCache.selectionAreas);
        if (isFocused())
            this.renderCursor(poseStack, displayCache.cursorPos, displayCache.cursorAtEnd);
    }

    private void renderHighlight(Rect2i[] selected) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);

        Color color = new Color(isFocused() ? selectionColor : selectionUnfocusedColor);

        RenderSystem.setShaderColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for (Rect2i rect2i : selected) {
            int i = this.x + rect2i.getX();
            int j = this.y + rect2i.getY();
            int k = i + rect2i.getWidth();
            int l = j + rect2i.getHeight();
            bufferBuilder.vertex(i, l, 0.0).endVertex();
            bufferBuilder.vertex(k, l, 0.0).endVertex();
            bufferBuilder.vertex(k, j, 0.0).endVertex();
            bufferBuilder.vertex(i, j, 0.0).endVertex();
        }

        tesselator.end();
        RenderSystem.disableColorLogicOp();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableTexture();
    }

    protected void renderCursor(@NotNull PoseStack poseStack, Pos2i cursorPos, boolean isEndOfText) {
        if (this.frameTick / 6 % 2 == 0) {
            cursorPos = convertLocalToScreen(cursorPos);
            if (isEndOfText)
                font.draw(poseStack, "_", cursorPos.x, cursorPos.y, getCurrentFontColor());
            else {
                poseStack.pushPose();
                poseStack.translate(0, 0, 50);
                RenderSystem.disableBlend();
                Screen.fill(poseStack, cursorPos.x, cursorPos.y - 1, cursorPos.x + 1, cursorPos.y + this.font.lineHeight, getCurrentFontColor());
                poseStack.popPose();
            }
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, createNarrationMessage());
    }

    @Override
    public @NotNull Component getMessage() {
        return Component.literal(getText());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused())
            return false;
        boolean handled = handleKeyPressed(keyCode, scanCode, modifiers);
        if (handled)
            clearDisplayCache();
        return handled;
    }

    protected boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {
        TextFieldHelper.CursorStep cursorStep = Screen.hasControlDown() ? TextFieldHelper.CursorStep.WORD : TextFieldHelper.CursorStep.CHARACTER;
        if (keyCode == InputConstants.KEY_UP) {
            changeLine(-1);
            return true;
        } else if (keyCode == InputConstants.KEY_DOWN) {
            changeLine(1);
            return true;
        } else if (keyCode == InputConstants.KEY_HOME) {
            keyHome();
            return true;
        } else if (keyCode == InputConstants.KEY_END) {
            keyEnd();
            return true;
        } else if (keyCode == InputConstants.KEY_BACKSPACE) {
            textFieldHelper.removeFromCursor(-1, cursorStep);
            return true;
        } else if (keyCode == InputConstants.KEY_DELETE) {
            textFieldHelper.removeFromCursor(1, cursorStep);
            return true;
        } else if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
            textFieldHelper.insertText(CommonComponents.NEW_LINE.getString());
            return true;
        }

        return textFieldHelper.keyPressed(keyCode);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!isFocused())
            return false;

        boolean typed = textFieldHelper.charTyped(codePoint);
        if (typed)
            clearDisplayCache();
        return typed;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered && visible && isActive() && button == 0) {
            long currentTime = Util.getMillis();
            DisplayCache displayCache = getDisplayCache();
            int index = displayCache.getIndexAtPosition(font, convertScreenToLocal(new Pos2i((int) mouseX, (int) mouseY)));

            if (index >= 0) {
                if (index == lastIndex && currentTime - lastClickTime < 250L) {
                    if (!textFieldHelper.isSelecting()) {
                        selectWord(index);
                    } else {
                        textFieldHelper.selectAll();
                    }
                } else {
                    textFieldHelper.setCursorPos(index, Screen.hasShiftDown());
                }
                clearDisplayCache();
            }

            lastIndex = index;
            lastClickTime = currentTime;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            DisplayCache displayCache = this.getDisplayCache();
            int index = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int) mouseX, (int) mouseY)));
            this.textFieldHelper.setCursorPos(index, true);
            this.clearDisplayCache();
        }
        return true;
    }

    public void setFocus(boolean focused) {
        setFocused(focused);
    }

    protected void selectWord(int index) {
        String string = this.getText();
        this.textFieldHelper.setSelectionRange(StringSplitter.getWordPosition(string, -1, index, false),
                StringSplitter.getWordPosition(string, 1, index, false));
    }

    protected void changeLine(int yChange) {
        int cursorPos = this.textFieldHelper.getCursorPos();
        int line = this.getDisplayCache().changeLine(cursorPos, yChange);
        this.textFieldHelper.setCursorPos(line, Screen.hasShiftDown());
    }

    protected void keyHome() {
        if (Screen.hasControlDown()) {
            this.textFieldHelper.setCursorToStart(Screen.hasShiftDown());
        } else {
            int cursorIndex = this.textFieldHelper.getCursorPos();
            int lineStartIndex = this.getDisplayCache().findLineStart(cursorIndex);
            this.textFieldHelper.setCursorPos(lineStartIndex, Screen.hasShiftDown());
        }
    }

    protected void keyEnd() {
        if (Screen.hasControlDown()) {
            this.textFieldHelper.setCursorToEnd(Screen.hasShiftDown());
        } else {
            DisplayCache displayCache = this.getDisplayCache();
            int cursorIndex = this.textFieldHelper.getCursorPos();
            int lineEndIndex = displayCache.findLineEnd(cursorIndex);
            this.textFieldHelper.setCursorPos(lineEndIndex, Screen.hasShiftDown());
        }
    }
}
