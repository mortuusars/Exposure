package io.github.mortuusars.exposure.client.gui.screen.element;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

@SuppressWarnings({"UnusedReturnValue", "BooleanMethodIsAlwaysInverted"})
public class Pager {
    public long lastChangedAt;
    public int changeCooldownMS = 50;
    public boolean playSound = true;
    public SoundEvent changeSoundEvent;

    protected ResourceLocation texture;
    protected int pages;
    protected boolean cycled;
    protected int currentPage;
    protected AbstractButton previousButton;
    protected AbstractButton nextButton;

    public Pager(SoundEvent changeSoundEvent) {
        this.changeSoundEvent = changeSoundEvent;
    }

    public void init(int pages, boolean cycled, AbstractButton previousPageButton, AbstractButton nextPageButton) {
        this.pages = pages;
        this.cycled = cycled;
        setPage(getCurrentPage());

        this.previousButton = previousPageButton;
        this.nextButton = nextPageButton;
        update();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setPage(int value) {
        this.currentPage = Mth.clamp(value, 0, this.pages);
    }

    public void update() {
        previousButton.visible = canChangePage(PagingDirection.PREVIOUS);
        nextButton.visible = canChangePage(PagingDirection.NEXT);
    }

    public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (Minecraft.getInstance().options.keyLeft.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_LEFT) {
            if (!isOnCooldown())
                changePage(PagingDirection.PREVIOUS);
            return true;
        }
        else if (Minecraft.getInstance().options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT) {
            if (!isOnCooldown())
                changePage(PagingDirection.NEXT);
            return true;
        }
        else
            return false;
    }

    public boolean handleKeyReleased(int keyCode, int scanCode, int modifiers) {
        if (Minecraft.getInstance().options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT
                || Minecraft.getInstance().options.keyLeft.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_LEFT) {
            lastChangedAt = 0;
            return true;
        }

        return false;
    }

    private boolean isOnCooldown() {
        return Util.getMillis() - lastChangedAt < changeCooldownMS;
    }

    public boolean canChangePage(PagingDirection direction) {
        int newIndex = getCurrentPage() + direction.getValue();
        return pages > 1 && (cycled || (0 <= newIndex && newIndex < pages));
    }

    public boolean changePage(PagingDirection direction) {
        if (!canChangePage(direction))
            return false;

        int oldPage = currentPage;
        int newPage = getCurrentPage() + direction.getValue();

        if (cycled && newPage >= pages)
            newPage = 0;
        else if (cycled && newPage < 0)
            newPage = pages - 1;

        if (oldPage == newPage)
            return false;

        setPage(newPage);
        onPageChanged(direction, oldPage, currentPage);
        return true;
    }

    public void onPageChanged(PagingDirection pagingDirection, int prevPage, int currentPage) {
        lastChangedAt = Util.getMillis();
        if (playSound)
            playChangeSound();
    }

    protected void playChangeSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                getChangeSound(), 0.8f, 1f));
    }

    protected SoundEvent getChangeSound() {
        return changeSoundEvent;
    }
}
