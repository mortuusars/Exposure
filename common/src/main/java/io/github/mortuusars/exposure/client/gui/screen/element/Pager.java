package io.github.mortuusars.exposure.client.gui.screen.element;

import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.util.Util;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Pager {
    protected int pagesCount;
    protected boolean cycle;
    protected int changeCooldownMs = 50;
    protected @Nullable SoundEffect changeSound = null;
    protected @Nullable AbstractButton previousPageButton = null;
    protected @Nullable AbstractButton nextPageButton = null;
    protected OnPageChanged onPageChanged = (oldPage, newPage) -> {
    };

    protected int currentPage;
    protected long lastChangeTime;

    // --

    public int getPagesCount() {
        return pagesCount;
    }

    public Pager setPagesCount(int pages) {
        this.pagesCount = pages;
        changePage(getPage()); // Updates current page to new range if needed
        return this;
    }

    public boolean isCycled() {
        return cycle;
    }

    public Pager setCycled(boolean cycled) {
        this.cycle = cycled;
        return this;
    }

    public int getChangeCooldown() {
        return changeCooldownMs;
    }

    public Pager setChangeCooldown(int milliseconds) {
        this.changeCooldownMs = milliseconds;
        return this;
    }

    public Optional<SoundEffect> getChangeSound() {
        return Optional.ofNullable(changeSound);
    }

    public Pager setChangeSound(@Nullable SoundEffect changeSound) {
        this.changeSound = changeSound;
        return this;
    }

    public Optional<AbstractButton> getPreviousPageButton() {
        return Optional.ofNullable(previousPageButton);
    }

    public Pager setPreviousPageButton(@Nullable AbstractButton previousPageButton) {
        this.previousPageButton = previousPageButton;
        updateButtonVisibility();
        return this;
    }

    public Optional<AbstractButton> getNextPageButton() {
        return Optional.ofNullable(nextPageButton);
    }

    public Pager setNextPageButton(@Nullable AbstractButton nextPageButton) {
        this.nextPageButton = nextPageButton;
        updateButtonVisibility();
        return this;
    }

    public OnPageChanged getOnPageChanged() {
        return onPageChanged;
    }

    public Pager onPageChanged(OnPageChanged onPageChanged) {
        this.onPageChanged = onPageChanged;
        return this;
    }

    // --

    public int getPage() {
        return currentPage;
    }

    public boolean changePage(int page) {
        page = Mth.clamp(page, 0, this.pagesCount);
        int oldPage = currentPage;
        if (currentPage == page) {
            return false;
        }
        currentPage = page;
        pageChanged(oldPage, page);
        return true;
    }

    /**
     * Sets page without side effects (except button visibility). Useful when setting up starting page.
     */
    public void setPage(int index) {
        currentPage = Mth.clamp(index, 0, this.pagesCount);
        updateButtonVisibility();
    }

    public boolean isOnCooldown() {
        return Util.getMillis() - lastChangeTime < changeCooldownMs;
    }

    public void resetCooldown() {
        lastChangeTime = 0;
    }

    public boolean isPagingDirectionAvailable(PagingDirection direction) {
        int newIndex = getPage() + direction.getValue();
        return pagesCount > 1 && (cycle || (0 <= newIndex && newIndex < pagesCount));
    }

    public boolean canChangePage(PagingDirection direction) {
        return isPagingDirectionAvailable(direction) && !isOnCooldown();
    }

    public boolean previousPage() {
        return changePage(PagingDirection.PREVIOUS);
    }

    public boolean nextPage() {
        return changePage(PagingDirection.NEXT);
    }

    public boolean changePage(PagingDirection direction) {
        if (!canChangePage(direction)) {
            return false;
        }

        int oldPage = currentPage;
        int newPage = getPage() + direction.getValue();

        if (cycle && newPage >= pagesCount)
            newPage = 0;
        else if (cycle && newPage < 0)
            newPage = pagesCount - 1;

        if (oldPage == newPage)
            return false;

        return changePage(newPage);
    }

    public void pageChanged(int oldPage, int newPage) {
        lastChangeTime = Util.getMillis();
        playChangeSound();
        updateButtonVisibility();
        onPageChanged.pageChanged(oldPage, newPage);
    }

    // --

    public void updateButtonVisibility() {
        if (previousPageButton != null) {
            previousPageButton.visible = isPagingDirectionAvailable(PagingDirection.PREVIOUS);
        }
        if (nextPageButton != null) {
            nextPageButton.visible = isPagingDirectionAvailable(PagingDirection.NEXT);
        }
    }

    public void playChangeSound() {
        getChangeSound().ifPresent(sound -> Minecrft.get().getSoundManager().play(
                SimpleSoundInstance.forUI(sound.get(), sound.getFinalPitch(), sound.volume())));
    }

    @FunctionalInterface
    public interface OnPageChanged {
        void pageChanged(int oldPage, int newPage);
    }
}
