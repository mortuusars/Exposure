package io.github.mortuusars.exposure.util;

import java.util.Objects;

public class Pos2i {
    public int x, y;

    public Pos2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pos2i pos2i = (Pos2i) o;
        return x == pos2i.x && y == pos2i.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Pos2i{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
