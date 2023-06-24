package io.github.mortuusars.exposure.item.attachment;

import io.github.mortuusars.exposure.item.FilmItem;
import net.minecraft.world.item.ItemStack;

public class Film {
    private final ItemStack filmStack;
    private final FilmItem filmItem;

    public Film(ItemStack filmStack) {
        this.filmStack = filmStack;

        if (filmStack.getItem() instanceof FilmItem filmItem)
            this.filmItem = filmItem;
        else
            throw new IllegalStateException(filmStack + " is not a FilmItem.");
    }

    public ItemStack getStack() {
        return filmStack;
    }

    public FilmItem getItem() {
        return filmItem;
    }
}
