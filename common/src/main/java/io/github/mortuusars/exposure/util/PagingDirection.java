package io.github.mortuusars.exposure.util;

public enum PagingDirection {
    PREVIOUS(-1),
    NEXT(1);

    private final int value;

    PagingDirection(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
