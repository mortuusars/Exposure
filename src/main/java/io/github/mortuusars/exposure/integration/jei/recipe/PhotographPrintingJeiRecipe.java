package io.github.mortuusars.exposure.integration.jei.recipe;

import io.github.mortuusars.exposure.camera.film.FilmType;

@SuppressWarnings("ClassCanBeRecord")
public class PhotographPrintingJeiRecipe {
    private final FilmType filmType;

    public PhotographPrintingJeiRecipe(FilmType filmType) {
        this.filmType = filmType;
    }

    public FilmType getFilmType() {
        return filmType;
    }
}
