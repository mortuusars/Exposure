package io.github.mortuusars.exposure.client.gui.screen.element;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.util.Pos2i;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TextBox extends AbstractWidget {
    public Predicate<String> textValidator = text -> text != null && getFont().wordWrapHeight(text, width) <= height;

    protected final Font font;
    @NotNull
    protected String text = "";
    public Supplier<String> textGetter = () -> text;
    public Consumer<String> textSetter = text -> this.text = text;

    protected final TextFieldHelper textFieldHelper;
    protected DisplayCache displayCache = null;
    public int fontColor = 0x00000000;
    public int fontUnfocusedColor = 0x00000000;
    public int selectionColor = 0xFF0000FF;
    public int selectionUnfocusedColor = 0x880000FF;
    protected int frameTick;
    protected long lastClickTime;
    protected int lastIndex = -1;

    public TextBox(@NotNull Font font, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        textFieldHelper = new TextFieldHelper(textGetter, textSetter,
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

    public void setHeight(int height) {
        this.height = height;
    }

    public @NotNull String getText() {
        return textGetter.get();
    }

    public TextBox setText(@NotNull String text) {
        textSetter.accept(text);
        return this;
    }

    public int getCurrentFontColor() {
        return isFocused() ? fontColor : fontUnfocusedColor;
    }

    public TextBox setFontColor(int fontColor, int fontUnfocusedColor) {
        this.fontColor = fontColor;
        this.fontUnfocusedColor = fontUnfocusedColor;
        return this;
    }

    public TextBox setSelectionColor(int selectionColor, int selectionUnfocusedColor) {
        this.selectionColor = selectionColor;
        this.selectionUnfocusedColor = selectionUnfocusedColor;
        return this;
    }

    public void setCursorToEnd() {
        textFieldHelper.setCursorToEnd();
    }

    protected boolean validateText(String text) {
        return textValidator.test(text);
    }

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
            this.textFieldHelper.removeFromCursor(-1, cursorStep);
            return true;
        } else if (keyCode == InputConstants.KEY_DELETE) {
            this.textFieldHelper.removeFromCursor(1, cursorStep);
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
            DisplayCache displayCache = this.getDisplayCache();
            int index = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int)mouseX, (int)mouseY)));
            if (index >= 0) {
                if (index == this.lastIndex && currentTime - this.lastClickTime < 250L) {
                    if (!this.textFieldHelper.isSelecting()) {
                        this.selectWord(index);
                    } else {
                        this.textFieldHelper.selectAll();
                    }
                } else {
                    this.textFieldHelper.setCursorPos(index, Screen.hasShiftDown());
                }
                this.clearDisplayCache();
            }
            this.lastIndex = index;
            this.lastClickTime = currentTime;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            DisplayCache displayCache = this.getDisplayCache();
            int index = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int)mouseX, (int)mouseY)));
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
            int i = this.textFieldHelper.getCursorPos();
            int j = this.getDisplayCache().findLineStart(i);
            this.textFieldHelper.setCursorPos(j, Screen.hasShiftDown());
        }
    }

    protected void keyEnd() {
        if (Screen.hasControlDown()) {
            this.textFieldHelper.setCursorToEnd(Screen.hasShiftDown());
        } else {
            DisplayCache displayCache = this.getDisplayCache();
            int i = this.textFieldHelper.getCursorPos();
            int j = displayCache.findLineEnd(i);
            this.textFieldHelper.setCursorPos(j, Screen.hasShiftDown());
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        DisplayCache displayCache = this.getDisplayCache();
        for (LineInfo lineInfo : displayCache.lines) {
            guiGraphics.drawString(this.font, lineInfo.asComponent, lineInfo.x, lineInfo.y, getCurrentFontColor(), false);
        }
        this.renderHighlight(guiGraphics, displayCache.selectionAreas);
        if (isFocused())
            this.renderCursor(guiGraphics, displayCache.cursor, displayCache.cursorAtEnd);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, text);
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

    protected void clearDisplayCache() {
        displayCache = null;
    }

    protected DisplayCache getDisplayCache() {
        if (this.displayCache == null)
            this.displayCache = this.rebuildDisplayCache();
        return this.displayCache;
    }

    protected DisplayCache rebuildDisplayCache() {
        String text = this.getText();

        if (text.isEmpty())
            return DisplayCache.EMPTY;

        int cursorPos = this.textFieldHelper.getCursorPos();
        int selectionPos = this.textFieldHelper.getSelectionPos();

        IntArrayList lineStarts = new IntArrayList();
        ArrayList<LineInfo> lines = Lists.newArrayList();
        MutableInt linesCount = new MutableInt();
        MutableBoolean mutableBoolean = new MutableBoolean();

        StringSplitter stringSplitter = this.font.getSplitter();
        stringSplitter.splitLines(text, getWidth(), Style.EMPTY, true, (style, x, y) -> {
            int lineIndex = linesCount.getAndIncrement();
            String textPart = text.substring(x, y);
            mutableBoolean.setValue(textPart.endsWith("\n"));
            textPart = StringUtils.stripEnd(textPart, " \n");
            int lineYPos = lineIndex * this.font.lineHeight;
            Pos2i linePos = this.convertLocalToScreen(new Pos2i(0, lineYPos));

            lineStarts.add(x);
            lines.add(new LineInfo(style, textPart, linePos.x, linePos.y));
        });

        int l;
        Pos2i newCursorPos;
        int[] lineStartsArray = lineStarts.toIntArray();
        boolean isCursorAtTextEnd = cursorPos == text.length();
        if (isCursorAtTextEnd && mutableBoolean.isTrue()) {
            newCursorPos = new Pos2i(0, lines.size() * this.font.lineHeight);
        } else {
            int k = findLineFromPos(lineStartsArray, cursorPos);
            l = this.font.width(text.substring(lineStartsArray[k], cursorPos));
            newCursorPos = new Pos2i(l, k * this.font.lineHeight);
        }
        ArrayList<Rect2i> list2 = Lists.newArrayList();
        if (cursorPos != selectionPos) {
            int o;
            l = Math.min(cursorPos, selectionPos);
            int m = Math.max(cursorPos, selectionPos);
            int n = findLineFromPos(lineStartsArray, l);
            if (n == (o = findLineFromPos(lineStartsArray, m))) {
                int p = n * this.font.lineHeight;
                int q = lineStartsArray[n];
                list2.add(this.createPartialLineSelection(text, stringSplitter, l, m, p, q));
            } else {
                int p = n + 1 > lineStartsArray.length ? text.length() : lineStartsArray[n + 1];
                list2.add(this.createPartialLineSelection(text, stringSplitter, l, p, n * this.font.lineHeight, lineStartsArray[n]));
                for (int q = n + 1; q < o; ++q) {
                    int r = q * this.font.lineHeight;
                    String string2 = text.substring(lineStartsArray[q], lineStartsArray[q + 1]);
                    int s = (int) stringSplitter.stringWidth(string2);
                    list2.add(this.createSelection(new Pos2i(0, r), new Pos2i(s, r + this.font.lineHeight)));
                }
                list2.add(this.createPartialLineSelection(text, stringSplitter, lineStartsArray[o], m, o * this.font.lineHeight, lineStartsArray[o]));
            }
        }
        return new DisplayCache(text, newCursorPos, isCursorAtTextEnd, lineStartsArray, lines.toArray(new LineInfo[0]), list2.toArray(new Rect2i[0]));
    }

    protected Rect2i createPartialLineSelection(String input, StringSplitter splitter, int startPos, int endPos, int y, int lineStart) {
        String string = input.substring(lineStart, startPos);
        String string2 = input.substring(lineStart, endPos);
        Pos2i pos2i = new Pos2i((int) splitter.stringWidth(string), y);
        Pos2i pos2i2 = new Pos2i((int) splitter.stringWidth(string2), y + this.font.lineHeight);
        return this.createSelection(pos2i, pos2i2);
    }

    protected Rect2i createSelection(Pos2i corner1, Pos2i corner2) {
        Pos2i pos2i = this.convertLocalToScreen(corner1);
        Pos2i pos2i2 = this.convertLocalToScreen(corner2);
        int i = Math.min(pos2i.x, pos2i2.x);
        int j = Math.max(pos2i.x, pos2i2.x);
        int k = Math.min(pos2i.y, pos2i2.y);
        int l = Math.max(pos2i.y, pos2i2.y);
        return new Rect2i(i, k, j - i, l - k);
    }

    protected int findLineFromPos(int[] lineStarts, int find) {
        int i = Arrays.binarySearch(lineStarts, find);
        if (i < 0) {
            return -(i + 2);
        }
        return i;
    }

    protected Pos2i convertLocalToScreen(Pos2i pos) {
        return new Pos2i(getX() + pos.x, getY() + pos.y);
    }

    protected Pos2i convertScreenToLocal(Pos2i screenPos) {
        return new Pos2i(screenPos.x - getX(), screenPos.y - getY());
    }

    public static class DisplayCache {
        static final DisplayCache EMPTY = new DisplayCache("", new Pos2i(0, 0), true,
                new int[]{0}, new LineInfo[]{new LineInfo(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
        protected final String fullText;
        final Pos2i cursor;
        final boolean cursorAtEnd;
        protected final int[] lineStarts;
        final LineInfo[] lines;
        final Rect2i[] selectionAreas;

        public DisplayCache(String fullText, Pos2i cursor, boolean cursorAtEnd, int[] lineStarts, LineInfo[] lines, Rect2i[] selection) {
            this.fullText = fullText;
            this.cursor = cursor;
            this.cursorAtEnd = cursorAtEnd;
            this.lineStarts = lineStarts;
            this.lines = lines;
            this.selectionAreas = selection;
        }

        public int getIndexAtPosition(Font font, Pos2i cursorPosition) {
            int i = cursorPosition.y / font.lineHeight;
            if (i < 0) {
                return 0;
            }
            if (i >= this.lines.length) {
                return this.fullText.length();
            }
            LineInfo lineInfo = this.lines[i];
            return this.lineStarts[i] + font.getSplitter()
                    .plainIndexAtWidth(lineInfo.contents, cursorPosition.x, lineInfo.style);
        }

        public int changeLine(int xChange, int yChange) {
            int m;
            int i = findLineFromPos(this.lineStarts, xChange);
            int j = i + yChange;
            if (0 <= j && j < this.lineStarts.length) {
                int k = xChange - this.lineStarts[i];
                int l = this.lines[j].contents.length();
                m = this.lineStarts[j] + Math.min(k, l);
            } else {
                m = xChange;
            }
            return m;
        }

        public int findLineStart(int line) {
            int i = findLineFromPos(this.lineStarts, line);
            return this.lineStarts[i];
        }

        public int findLineEnd(int line) {
            int i = findLineFromPos(this.lineStarts, line);
            return this.lineStarts[i] + this.lines[i].contents.length();
        }

        protected int findLineFromPos(int[] lineStarts, int find) {
            int i = Arrays.binarySearch(lineStarts, find);
            if (i < 0) {
                return -(i + 2);
            }
            return i;
        }
    }

    public static class LineInfo {
        final Style style;
        final String contents;
        final Component asComponent;
        final int x;
        final int y;

        public LineInfo(Style style, String contents, int x, int y) {
            this.style = style;
            this.contents = contents;
            this.x = x;
            this.y = y;
            this.asComponent = Component.literal(contents).setStyle(style);
        }
    }
}
