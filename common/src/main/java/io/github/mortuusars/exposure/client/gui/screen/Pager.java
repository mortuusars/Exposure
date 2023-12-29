package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class Pager {
    public static final int BUTTON_SIZE = 16;

    public long lastChangedAt;
    public int changeCooldownMS = 50;
    public boolean playSound = true;

    protected ResourceLocation texture;
    protected int pages;
    protected boolean cycled;
    protected int currentPage;
    protected ImageButton previousButton;
    protected ImageButton nextButton;

    public Pager(ResourceLocation texture) {
        this.texture = texture;
    }

    public int getCurrentPageIndex() {
        return currentPage;
    }

    public void init(int screenWidth, int screenHeight, int pages, boolean cycled, Consumer<AbstractButton> addButtonAction) {
        this.pages = pages;
        this.cycled = cycled;
        previousButton = new ImageButton(0, (int) (screenHeight / 2f - BUTTON_SIZE / 2f), BUTTON_SIZE, BUTTON_SIZE,
                0, 0, BUTTON_SIZE, texture, button -> onPreviousButtonPressed());
        nextButton = new ImageButton(screenWidth - BUTTON_SIZE, (int) (screenHeight / 2f - BUTTON_SIZE / 2f), BUTTON_SIZE, BUTTON_SIZE,
                16, 0, BUTTON_SIZE, texture, button -> onNextButtonPressed());

        addButtonAction.accept(previousButton);
        addButtonAction.accept(nextButton);

        update();
    }

    public void update() {
        previousButton.visible = pages > 1 && (cycled || currentPage > 0);
        nextButton.visible = pages > 1 && (cycled || currentPage < pages - 1);
    }

    public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (Minecraft.getInstance().options.keyLeft.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_LEFT) {
            changePage(PagingDirection.PREVIOUS);
            return true;
        }
        else if (Minecraft.getInstance().options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT) {
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

    protected void onPreviousButtonPressed() {
        changePage(PagingDirection.PREVIOUS);
    }

    protected void onNextButtonPressed() {
        changePage(PagingDirection.NEXT);
    }

    public void changePage(PagingDirection pagingDirection) {
        if (pages < 2 || Util.getMillis() - lastChangedAt < changeCooldownMS)
            return;

        int prevIndex = currentPage;

        currentPage += pagingDirection == PagingDirection.NEXT ? 1 : -1;

        if (cycled && currentPage >= pages)
            currentPage = 0;
        else if (cycled && currentPage < 0)
            currentPage = pages - 1;
        else if (!cycled)
            currentPage = Mth.clamp(currentPage, 0, pages - 1);

        if (prevIndex != currentPage) {
            lastChangedAt = Util.getMillis();
            if (playSound)
                playChangeSound();
        }
    }

    protected void playChangeSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                getChangeSound(), 0.8f, 1f));
    }

    protected SoundEvent getChangeSound() {
        return Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get();
    }
}
