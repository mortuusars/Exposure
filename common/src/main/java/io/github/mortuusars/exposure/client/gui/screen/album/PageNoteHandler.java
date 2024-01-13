package io.github.mortuusars.exposure.client.gui.screen.album;

import io.github.mortuusars.exposure.menu.AlbumMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextFieldHelper;

public class PageNoteHandler {
    private final AlbumMenu.Page page;
    private final AlbumScreen screen;
    private final Font font;

    private String text = "";

    private final TextFieldHelper textEdit = new TextFieldHelper(
            this::getNoteText,
            this::setNoteText,
            () -> TextFieldHelper.getClipboardContents(Minecraft.getInstance()),
            text -> TextFieldHelper.setClipboardContents(Minecraft.getInstance(), text),
            this::validateText);

    private boolean validateText(String text) {
        return text != null && text.length() < 512 && getFont().wordWrapHeight(text,
                AlbumScreen.MAX_NOTE_WIDTH) <= getFont().lineHeight * AlbumScreen.MAX_NOTE_LINES;
    }

    public PageNoteHandler(AlbumMenu.Page page, AlbumScreen screen, Font font) {
        this.page = page;
        this.screen = screen;
        this.font = font;
    }

    private Font getFont() {
        return font;
    }

    public String getNoteText() {
        return text;
    }

    private void setNoteText(String text) {
        this.text = text;
    }

    public boolean handleInput(int keyCode, int scanCode, int modifiers) {
        return textEdit.keyPressed(keyCode);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return textEdit.charTyped(codePoint);
    }
}
