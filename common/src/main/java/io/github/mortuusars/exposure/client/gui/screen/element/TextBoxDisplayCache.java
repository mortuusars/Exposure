package io.github.mortuusars.exposure.client.gui.screen.element;

import com.google.common.collect.Lists;
import io.github.mortuusars.exposure.util.Pos2i;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Arrays;

public class TextBoxDisplayCache {
    public static final TextBoxDisplayCache EMPTY = new TextBoxDisplayCache("", new Pos2i(0, 0), true,
            new int[]{0}, new LineInfo[]{LineInfo.EMPTY}, new Rect2i[0]);

    public String fullText;
    public Pos2i cursorPos;
    public boolean cursorAtEnd;
    public int[] lineStarts;
    public LineInfo[] lines;
    public Rect2i[] selectionAreas;

    public boolean needsRebuilding = true;

    public TextBoxDisplayCache(String fullText, Pos2i cursor, boolean cursorAtEnd, int[] lineStarts, LineInfo[] lines, Rect2i[] selection) {
        this.fullText = fullText;
        this.cursorPos = cursor;
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

        return this.lineStarts[i] + font.getSplitter().plainIndexAtWidth(lineInfo.contents, cursorPosition.x, lineInfo.style);
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

    public void rebuild(Font font, String text, int cursorPos, int selectionPos, int x, int y, int width, int height) {
        if (text.isEmpty()) {
            this.fullText = "";
            this.cursorPos = new Pos2i(0, 0);
            this.cursorAtEnd = true;
            this.lineStarts = new int[]{0};
            this.lines = new LineInfo[]{LineInfo.EMPTY};
            this.selectionAreas = new Rect2i[0];
        }

        IntArrayList lineStartIndexes = new IntArrayList();
        ArrayList<LineInfo> lines = Lists.newArrayList();
        MutableInt linesCount = new MutableInt();
        MutableBoolean endsOnNewLine = new MutableBoolean();

        StringSplitter stringSplitter = font.getSplitter();
        stringSplitter.splitLines(text, width, Style.EMPTY, true, (style, lineStartIndex, lineEndIndex) -> {
            int lineIndex = linesCount.getAndIncrement();
            String lineText = text.substring(lineStartIndex, lineEndIndex);
            endsOnNewLine.setValue(lineText.endsWith("\n"));
            lineText = StringUtils.stripEnd(lineText, " \n");
            int lineYPos = lineIndex * font.lineHeight;

//            int lineX = getWidth() / 2 - font.width(lineText) / 2;

            Pos2i linePos = convertLocalToScreen(new Pos2i(0, lineYPos), x, y);

            lineStartIndexes.add(lineStartIndex);
            lines.add(new TextBoxDisplayCache.LineInfo(style, lineText, linePos.x, linePos.y));
        });

        int cursorX;
        Pos2i newCursorPos;
        int[] lineStartIndexesArray = lineStartIndexes.toIntArray();
        boolean isCursorAtTextEnd = cursorPos == text.length();

        if (isCursorAtTextEnd && endsOnNewLine.isTrue()) {
            newCursorPos = new Pos2i(/*getWidth() / 2*/0, lines.size() * font.lineHeight);
        }
        else if (lines.isEmpty()) {
            newCursorPos = new Pos2i(0, 0);
        } else {
            int lineIndex = findLineFromPos(lineStartIndexesArray, cursorPos);

            String lineTextToCursor = text.substring(lineStartIndexesArray[lineIndex], cursorPos);
            cursorX = /*getWidth() / 2 + font.width(lineTextToCursor) / 2*/ font.width(lineTextToCursor);
            newCursorPos = new Pos2i(cursorX, lineIndex * font.lineHeight);
        }

        ArrayList<Rect2i> selections = Lists.newArrayList();
        if (cursorPos != selectionPos) {
            int o;
            cursorX = Math.min(cursorPos, selectionPos);
            int m = Math.max(cursorPos, selectionPos);
            int lineAtCursor = findLineFromPos(lineStartIndexesArray, cursorX);

            if (lineAtCursor == (o = findLineFromPos(lineStartIndexesArray, m))) {
                int cursorLineY = lineAtCursor * font.lineHeight;
                int lineX = lineStartIndexesArray[lineAtCursor];
                selections.add(createPartialLineSelection(font, text, stringSplitter, cursorX, m, cursorLineY, lineX, x, y));
            } else {
                int p = lineAtCursor + 1 > lineStartIndexesArray.length ? text.length() : lineStartIndexesArray[lineAtCursor + 1];
                selections.add(createPartialLineSelection(font, text, stringSplitter, cursorX, p, lineAtCursor * font.lineHeight, lineStartIndexesArray[lineAtCursor], x, y));
                for (int lineI = lineAtCursor + 1; lineI < o; ++lineI) {
                    int selectionY = lineI * font.lineHeight;
                    String string2 = text.substring(lineStartIndexesArray[lineI], lineStartIndexesArray[lineI + 1]);
                    int selectionWidth = (int) stringSplitter.stringWidth(string2);
                    selections.add(createSelection(new Pos2i(0, selectionY), new Pos2i(selectionWidth, selectionY + font.lineHeight), x, y));
                }

                selections.add(createPartialLineSelection(font, text, stringSplitter, lineStartIndexesArray[o], m, o * font.lineHeight, lineStartIndexesArray[o], x, y));
            }
        }

        this.fullText = text;
        this.cursorPos = newCursorPos;
        this.cursorAtEnd = isCursorAtTextEnd;
        this.lineStarts = lineStartIndexesArray;
        this.lines = lines.toArray(TextBoxDisplayCache.LineInfo[]::new);
        this.selectionAreas = selections.toArray(Rect2i[]::new);
    }

    protected Rect2i createPartialLineSelection(Font font, String input, StringSplitter splitter, int startPos, int endPos, int y, int lineStart, int boxX, int boxY) {
        String string = input.substring(lineStart, startPos);
        String string2 = input.substring(lineStart, endPos);
        Pos2i corner1 = new Pos2i((int) splitter.stringWidth(string), y);
        Pos2i corner2 = new Pos2i((int) splitter.stringWidth(string2), y + font.lineHeight);
        return this.createSelection(corner1, corner2, boxX, boxY);
    }

    protected Rect2i createSelection(Pos2i corner1, Pos2i corner2, int x, int y) {
        Pos2i c1 = convertLocalToScreen(corner1, x, y);
        Pos2i c2 = convertLocalToScreen(corner2, x, y);
        int x1 = Math.min(c1.x, c2.x);
        int x2 = Math.max(c1.x, c2.x);
        int y1 = Math.min(c1.y, c2.y);
        int y2 = Math.max(c1.y, c2.y);
        return new Rect2i(x1, y1, x2 - x1, y2 - y1);
    }

    protected Pos2i convertLocalToScreen(Pos2i pos, int x, int y) {
        return new Pos2i(x + pos.x, y + pos.y);
    }

    protected Pos2i convertScreenToLocal(Pos2i screenPos, int x, int y) {
        return new Pos2i(screenPos.x - x, screenPos.y - y);
    }

    public static class LineInfo {
        public static final LineInfo EMPTY = new LineInfo(Style.EMPTY, "", 0, 0);

        public final Style style;
        public final String contents;
        public final Component asComponent;
        public final int x;
        public final int y;

        public LineInfo(Style style, String contents, int x, int y) {
            this.style = style;
            this.contents = contents;
            this.x = x;
            this.y = y;
            this.asComponent = Component.literal(contents).setStyle(style);
        }
    }
}
