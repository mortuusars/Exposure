package io.github.mortuusars.exposure.gui.screen.element.textbox;

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
import java.util.Collections;
import java.util.List;

public class DisplayCache {
    public String fullText = "";
    public Pos2i cursorPos = new Pos2i(0, 0);
    public boolean cursorAtEnd = true;
    public int[] lineStarts = new int[]{0};
    public LineInfo[] lines = new LineInfo[]{LineInfo.EMPTY};
    public Rect2i[] selectionAreas = new Rect2i[0];

    public boolean needsRebuilding = true;
    public int x;
    public int y;
    public int width;
    public int height;
    public HorizontalAlignment alignment = HorizontalAlignment.LEFT;

    public int getIndexAtPosition(Font font, Pos2i cursorPosition) {
        int lineIndex = cursorPosition.y / font.lineHeight;

        if (lineIndex < 0)
            return 0;
        else if (lineIndex >= this.lines.length)
            return this.fullText.length();

        LineInfo lineInfo = this.lines[lineIndex];

        return this.lineStarts[lineIndex] + font.getSplitter()
                .plainIndexAtWidth(lineInfo.contents, cursorPosition.x - lineInfo.x, lineInfo.style);
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

    public void rebuild(Font font, String text, int cursorIndex, int selectionIndex, int x, int y, int width, int height,
                        HorizontalAlignment alignment) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.alignment = alignment;

        this.needsRebuilding = false;

        if (text.isEmpty()) {
            this.fullText = "";
            this.cursorPos = new Pos2i(alignment.align(width, font.width("_")), 0);
            this.cursorAtEnd = true;
            this.lineStarts = new int[]{0};
            this.lines = new LineInfo[]{LineInfo.EMPTY};
            this.selectionAreas = new Rect2i[0];
            return;
        }

        // Cursor can be
//        cursorIndex = Math.min(cursorIndex, text.length() - 1);

        IntArrayList lineStartIndexes = new IntArrayList();
        List<LineInfo> lines = new ArrayList<>();
        MutableInt linesCount = new MutableInt();
        MutableBoolean endsOnNewLine = new MutableBoolean();

        StringSplitter stringSplitter = font.getSplitter();
        stringSplitter.splitLines(text, width, Style.EMPTY, true, (style, lineStartIndex, lineEndIndex) -> {
            int lineIndex = linesCount.getAndIncrement();
            String lineText = text.substring(lineStartIndex, lineEndIndex);
            endsOnNewLine.setValue(lineText.endsWith("\n"));
            lineText = StringUtils.stripEnd(lineText, "\n");

            int contentWidth = (int) stringSplitter.stringWidth(lineText);
            int lineXPos = alignment.align(width, contentWidth);
            int lineYPos = lineIndex * font.lineHeight;

            lineStartIndexes.add(lineStartIndex);

            lines.add(new DisplayCache.LineInfo(style, lineText, lineXPos, lineYPos, contentWidth, font.lineHeight));
        });

        this.fullText = text;
        this.lines = lines.toArray(DisplayCache.LineInfo[]::new);;

        int cursorX;
        Pos2i newCursorPos;
        int[] lineStartIndexesArray = lineStartIndexes.toIntArray();
        boolean isCursorAtTextEnd = cursorIndex == text.length();

        if (isCursorAtTextEnd && endsOnNewLine.isTrue()) {
            newCursorPos = new Pos2i(alignment.align(this.width, 0), lines.size() * font.lineHeight);
        } else {
            int lineIndex = findLineFromPos(lineStartIndexesArray, cursorIndex);
            LineInfo line = lines.get(lineIndex);

            String lineTextToCursor = text.substring(lineStartIndexesArray[lineIndex], cursorIndex);

            cursorX = line.x + (int)stringSplitter.stringWidth(lineTextToCursor);
            newCursorPos = new Pos2i(cursorX, line.y);
        }

        List<Rect2i> selections = cursorIndex != selectionIndex ?
                createSelectionAreas(font, text, this.lines, cursorIndex, selectionIndex, stringSplitter, lineStartIndexesArray)
                : Collections.emptyList();

        this.cursorPos = newCursorPos;
        this.cursorAtEnd = isCursorAtTextEnd;
        this.lineStarts = lineStartIndexesArray;
        this.selectionAreas = selections.toArray(Rect2i[]::new);
    }

    private List<Rect2i> createSelectionAreas(Font font, String fullText, LineInfo[] lines, int cursorPos, int selectionPos, StringSplitter stringSplitter, int[] lineStartIndexesArray) {
        int startIndex = Math.min(cursorPos, selectionPos);
        int endIndex = Math.max(cursorPos, selectionPos);
        int lineAtStart = findLineFromPos(lineStartIndexesArray, startIndex);
        int lineAtEnd = findLineFromPos(lineStartIndexesArray, endIndex);

        List<Rect2i> areas = new ArrayList<>();


        for (int lineIndex = lineAtStart; lineIndex <= lineAtEnd; lineIndex++) {
            LineInfo line = lines[lineIndex];
            int lineStartIndex = lineStartIndexesArray[lineIndex];

            String selectedText = fullText.substring(
                    Math.max(startIndex, lineStartIndex),
                    lineIndex == lineAtEnd ? endIndex : lineStartIndexesArray[lineIndex + 1]);

            int selectionWidth = (int) stringSplitter.stringWidth(selectedText);

            String unselectedText = fullText.substring(lineStartIndex, Math.max(startIndex, lineStartIndex));
            int selectionX = startIndex > lineStartIndex ? (int)stringSplitter.stringWidth(unselectedText) : 0;

            areas.add(new Rect2i(line.x + selectionX, line.y, selectionWidth, font.lineHeight));
        }

        return areas;
    }

    public static class LineInfo {
        public static final LineInfo EMPTY = new LineInfo(Style.EMPTY, "", 0, 0, 0, 0);

        public final Style style;
        public final String contents;
        public final Component asComponent;
        public final int x;
        public final int y;
        public final int width;
        public final int height;

        public LineInfo(Style style, String contents, int x, int y, int width, int height) {
            this.style = style;
            this.contents = contents;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.asComponent = Component.literal(contents).setStyle(style);
        }
    }
}
