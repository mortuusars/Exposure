package io.github.mortuusars.exposure.client.gui.screen.element.textbox;

public enum HorizontalAlignment {
    LEFT,
    CENTER,
    RIGHT;

    public int align(int areaWidth, int contentWidth) {
        if (this == CENTER)
            return areaWidth / 2 - contentWidth / 2;
        else if (this == RIGHT)
            return areaWidth - contentWidth;
        else
            return 0;
    }
}
