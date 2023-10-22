package io.github.mortuusars.exposure.integration.jei.recipe;

@SuppressWarnings("ClassCanBeRecord")
public class PhotographStackingJeiRecipe {
    public static final int STACKING = 0;
    public static final int REMOVING = 1;

    private final int type;

    public PhotographStackingJeiRecipe(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
