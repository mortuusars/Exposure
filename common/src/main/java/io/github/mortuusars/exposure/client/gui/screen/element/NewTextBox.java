package io.github.mortuusars.exposure.client.gui.screen.element;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.util.Pos2i;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class NewTextBox extends AbstractWidget {
    public final Font font;
    public Supplier<String> textGetter;
    public Consumer<String> textSetter;
    public Predicate<String> textValidator = Objects::nonNull/* && getFont().wordWrapHeight(text, width) <= height*/;

    public HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    public int fontColor = 0xFF000000;
    public int fontUnfocusedColor = 0xFF000000;
    public int selectionColor = 0xFF0000FF;
    public int selectionUnfocusedColor = 0x880000FF;

    public final TextFieldHelper textFieldHelper;
    protected TextBoxDisplayCache displayCache = TextBoxDisplayCache.EMPTY;
    protected int frameTick;
    protected long lastClickTime;
    protected int lastIndex = -1;

    public NewTextBox(@NotNull Font font, int x, int y, int width, int height,
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

    public @NotNull String getText() {
        return textGetter.get();
    }

    public NewTextBox setText(@NotNull String text) {
        textSetter.accept(text);
        return this;
    }

    protected boolean validateText(String text) {
        return textValidator.test(text);
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getCurrentFontColor() {
        return isFocused() ? fontColor : fontUnfocusedColor;
    }

    public NewTextBox setFontColor(int fontColor, int fontUnfocusedColor) {
        this.fontColor = fontColor;
        this.fontUnfocusedColor = fontUnfocusedColor;
        return this;
    }

    public NewTextBox setSelectionColor(int selectionColor, int selectionUnfocusedColor) {
        this.selectionColor = selectionColor;
        this.selectionUnfocusedColor = selectionUnfocusedColor;
        return this;
    }

    public void setCursorToEnd() {
        textFieldHelper.setCursorToEnd();
    }

    public void refresh() {
        clearDisplayCache();
    }

    protected TextBoxDisplayCache getDisplayCache() {
        if (displayCache.needsRebuilding)
            displayCache.rebuild(font, getText(), textFieldHelper.getCursorPos(), textFieldHelper.getSelectionPos(),
                    getX(), getY(), getWidth(), getHeight());
        return displayCache;
    }

    protected void clearDisplayCache() {
        displayCache.needsRebuilding = true;
    }

//    protected TextBoxDisplayCache rebuildDisplayCache() {
//        String text = getText();
//
//        if (text.isEmpty())
//            return TextBoxDisplayCache.EMPTY;
//
//        int cursorPos = textFieldHelper.getCursorPos();
//        int selectionPos = textFieldHelper.getSelectionPos();
//
//        IntArrayList lineStartIndexes = new IntArrayList();
//        ArrayList<TextBoxDisplayCache.LineInfo> lines = Lists.newArrayList();
//        MutableInt linesCount = new MutableInt();
//        MutableBoolean endsOnNewLine = new MutableBoolean();
//
//        StringSplitter stringSplitter = font.getSplitter();
//        stringSplitter.splitLines(text, getWidth(), Style.EMPTY, true, (style, x, y) -> {
//            int lineIndex = linesCount.getAndIncrement();
//            String lineText = text.substring(x, y);
//            endsOnNewLine.setValue(lineText.endsWith("\n"));
//            lineText = StringUtils.stripEnd(lineText, " \n");
//            int lineYPos = lineIndex * font.lineHeight;
//
////            int lineX = getWidth() / 2 - font.width(lineText) / 2;
//
//            Pos2i linePos = convertLocalToScreen(new Pos2i(0, lineYPos));
//
//            lineStartIndexes.add(x);
//            lines.add(new TextBoxDisplayCache.LineInfo(style, lineText, linePos.x, linePos.y));
//        });
//
//        int cursorX;
//        Pos2i newCursorPos;
//        int[] lineStartIndexesArray = lineStartIndexes.toIntArray();
//        boolean isCursorAtTextEnd = cursorPos == text.length();
//
//        if (isCursorAtTextEnd && endsOnNewLine.isTrue()) {
//            newCursorPos = new Pos2i(/*getWidth() / 2*/0, lines.size() * font.lineHeight);
//        } else {
//            int lineIndex = findLineFromPos(lineStartIndexesArray, cursorPos);
//            String lineTextToCursor = text.substring(lineStartIndexesArray[lineIndex], cursorPos);
//            cursorX = /*getWidth() / 2 + font.width(lineTextToCursor) / 2*/ font.width(lineTextToCursor);
//            newCursorPos = new Pos2i(cursorX, lineIndex * font.lineHeight);
//        }
//
//        ArrayList<Rect2i> selections = Lists.newArrayList();
//        if (cursorPos != selectionPos) {
//            int o;
//            cursorX = Math.min(cursorPos, selectionPos);
//            int m = Math.max(cursorPos, selectionPos);
//            int lineAtCursor = findLineFromPos(lineStartIndexesArray, cursorX);
//
//            if (lineAtCursor == (o = findLineFromPos(lineStartIndexesArray, m))) {
//                int cursorLineY = lineAtCursor * font.lineHeight;
//                int lineX = lineStartIndexesArray[lineAtCursor];
//                selections.add(createPartialLineSelection(text, stringSplitter, cursorX, m, cursorLineY, lineX));
//            } else {
//                int p = lineAtCursor + 1 > lineStartIndexesArray.length ? text.length() : lineStartIndexesArray[lineAtCursor + 1];
//                selections.add(createPartialLineSelection(text, stringSplitter, cursorX, p, lineAtCursor * font.lineHeight, lineStartIndexesArray[lineAtCursor]));
//                for (int lineI = lineAtCursor + 1; lineI < o; ++lineI) {
//                    int selectionY = lineI * font.lineHeight;
//                    String string2 = text.substring(lineStartIndexesArray[lineI], lineStartIndexesArray[lineI + 1]);
//                    int selectionWidth = (int) stringSplitter.stringWidth(string2);
//                    selections.add(createSelection(new Pos2i(0, selectionY), new Pos2i(selectionWidth, selectionY + font.lineHeight)));
//                }
//
//                selections.add(createPartialLineSelection(text, stringSplitter, lineStartIndexesArray[o], m, o * font.lineHeight, lineStartIndexesArray[o]));
//            }
//        }
//
//        return new TextBoxDisplayCache(text, newCursorPos, isCursorAtTextEnd, lineStartIndexesArray,
//                lines.toArray(TextBoxDisplayCache.LineInfo[]::new), selections.toArray(Rect2i[]::new));
//    }

//    protected Rect2i createPartialLineSelection(String input, StringSplitter splitter, int startPos, int endPos, int y, int lineStart) {
//        String string = input.substring(lineStart, startPos);
//        String string2 = input.substring(lineStart, endPos);
//        Pos2i pos2i = new Pos2i((int) splitter.stringWidth(string), y);
//        Pos2i pos2i2 = new Pos2i((int) splitter.stringWidth(string2), y + this.font.lineHeight);
//        return this.createSelection(pos2i, pos2i2);
//    }

//    protected Rect2i createSelection(Pos2i corner1, Pos2i corner2) {
//        Pos2i pos2i = this.convertLocalToScreen(corner1);
//        Pos2i pos2i2 = this.convertLocalToScreen(corner2);
//        int i = Math.min(pos2i.x, pos2i2.x);
//        int j = Math.max(pos2i.x, pos2i2.x);
//        int k = Math.min(pos2i.y, pos2i2.y);
//        int l = Math.max(pos2i.y, pos2i2.y);
//        return new Rect2i(i, k, j - i, l - k);
//    }

//    protected int findLineFromPos(int[] lineStarts, int find) {
//        int i = Arrays.binarySearch(lineStarts, find);
//        if (i < 0) {
//            return -(i + 2);
//        }
//        return i;
//    }

    protected Pos2i convertLocalToScreen(Pos2i pos) {
        return new Pos2i(getX() + pos.x, getY() + pos.y);
    }

    protected Pos2i convertScreenToLocal(Pos2i screenPos) {
        return new Pos2i(screenPos.x - getX(), screenPos.y - getY());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        TextBoxDisplayCache displayCache = this.getDisplayCache();
        for (TextBoxDisplayCache.LineInfo lineInfo : displayCache.lines) {
            guiGraphics.drawString(this.font, lineInfo.asComponent, lineInfo.x, lineInfo.y, getCurrentFontColor(), false);
        }
        this.renderHighlight(guiGraphics, displayCache.selectionAreas);
        if (isFocused())
            this.renderCursor(guiGraphics, displayCache.cursorPos, displayCache.cursorAtEnd);
    }

    protected void renderHighlight(GuiGraphics guiGraphics, Rect2i[] highlightAreas) {
        for (Rect2i selection : highlightAreas) {
            int x = selection.getX();
            int y = selection.getY();
            int x1 = x + selection.getWidth();
            int y1 = y + selection.getHeight();
            guiGraphics.fill(RenderType.guiTextHighlight(), x, y - 1, x1, y1, isFocused() ? selectionColor : selectionUnfocusedColor);
        }
    }

    protected void renderCursor(GuiGraphics guiGraphics, Pos2i cursorPos, boolean isEndOfText) {
        if (this.frameTick / 6 % 2 == 0) {
            cursorPos = convertLocalToScreen(cursorPos);
            if (isEndOfText)
                guiGraphics.drawString(this.font, "_", cursorPos.x, cursorPos.y, getCurrentFontColor(), false);
            else {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 50);
                RenderSystem.disableBlend();
                guiGraphics.fill(cursorPos.x, cursorPos.y - 1, cursorPos.x + 1, cursorPos.y + this.font.lineHeight, getCurrentFontColor());
                guiGraphics.pose().popPose();
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
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
            TextBoxDisplayCache displayCache = getDisplayCache();
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
            TextBoxDisplayCache displayCache = this.getDisplayCache();
            int index = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int) mouseX, (int) mouseY)));
            this.textFieldHelper.setCursorPos(index, true);
            this.clearDisplayCache();
        }
        return true;
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
            TextBoxDisplayCache displayCache = this.getDisplayCache();
            int cursorIndex = this.textFieldHelper.getCursorPos();
            int lineEndIndex = displayCache.findLineEnd(cursorIndex);
            this.textFieldHelper.setCursorPos(lineEndIndex, Screen.hasShiftDown());
        }
    }
}
