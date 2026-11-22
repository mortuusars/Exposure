package io.github.mortuusars.exposure.client.gui.component;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.gui.Tooltips;
import io.github.mortuusars.exposure.client.gui.Widgets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class CycleButton<T> extends Button {
    protected final List<T> values;
    protected final T startingValue;
    protected final Map<T, WidgetSprites> spritesMap;

    protected OnCycle<T> onCycle;
    protected boolean loop = true;
    protected @Nullable Tooltip defaultTooltip;
    protected Map<T, @Nullable Tooltip> tooltips = Collections.emptyMap();
    protected @Nullable SoundEvent clickSound;

    protected int currentIndex;

    public CycleButton(int x, int y, int width, int height, List<T> values, @NotNull T startingValue,
                       Map<T, WidgetSprites> spritesMap, OnCycle<T> onCycle) {
        super(x, y, width, height, CommonComponents.EMPTY, b -> {}, DEFAULT_NARRATION);
        Preconditions.checkArgument(!values.isEmpty(), "Cannot create a CycleButton with 0 possible values.");
        this.values = values;
        this.startingValue = startingValue;
        Preconditions.checkArgument(!spritesMap.isEmpty(), "Cannot create a CycleButton with 0 sprites.");
        this.spritesMap = spritesMap;
        this.onCycle = onCycle;

        setCurrentIndex(Math.max(values.indexOf(startingValue), 0));
    }

    public CycleButton(int x, int y, int width, int height, List<T> values, @NotNull T startingValue,
                       Map<T, WidgetSprites> spritesMap) {
        this(x, y, width, height, values, startingValue, spritesMap, (b, v) -> {});
    }

    public CycleButton(int x, int y, int width, int height, List<T> values, @NotNull T startingValue,
                       Function<T, WidgetSprites> spritesFunc, OnCycle<T> onCycle) {
        this(x, y, width, height, values, startingValue, Widgets.createMap(values, spritesFunc), onCycle);
    }

    public CycleButton(int x, int y, int width, int height, List<T> values, @NotNull T startingValue,
                       Function<T, WidgetSprites> spritesFunc) {
        this(x, y, width, height, values, startingValue, Widgets.createMap(values, spritesFunc), (b, v) -> {});
    }

    public CycleButton<T> setLooping(boolean loop) {
        this.loop = loop;
        return this;
    }

    public CycleButton<T> setDefaultTooltip(Tooltip tooltip) {
        this.defaultTooltip = tooltip;
        updateVisuals();
        return this;
    }

    public CycleButton<T> setTooltips(Map<T, Tooltip> tooltips) {
        this.tooltips = tooltips;
        updateVisuals();
        return this;
    }

    public CycleButton<T> setTooltips(Function<T, Component> tooltipFunc) {
        this.tooltips = Tooltips.createMap(values, tooltipFunc);
        updateVisuals();
        return this;
    }

    public CycleButton<T> setClickSound(SoundEvent soundEvent) {
        this.clickSound = soundEvent;
        return this;
    }

    public CycleButton<T> onCycle(OnCycle<T> onCycle) {
        this.onCycle = onCycle;
        return this;
    }

    public CycleButton<T> onCycle(Consumer<T> onCycle) {
        this.onCycle = (button, newValue) -> onCycle.accept(newValue);
        return this;
    }

    public T getCurrentValue() {
        return values.get(Mth.clamp(currentIndex, 0, values.size() - 1));
    }

    public void setCurrentValue(T value) {
        setCurrentIndex(Mth.clamp(values.indexOf(value), 0, values.size() - 1));
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
        updateVisuals();
    }

    public void cycle(boolean reverse) {
        int value = currentIndex;
        value += reverse ? -1 : 1;
        if (value < 0)
            value = loop ? values.size() - 1 : 0;
        else if (value >= values.size())
            value = loop ? 0 : values.size() - 1;

        if (currentIndex != value) {
            currentIndex = value;
            onCycle();
        }
    }

    protected void onCycle() {
        updateVisuals();
        onCycle.onCycle(this, getCurrentValue());
    }

    private void updateVisuals() {
        @Nullable Tooltip tooltip = tooltips.getOrDefault(getCurrentValue(), defaultTooltip);
        if (tooltip != null) {
            setTooltip(tooltip);
        }
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        @Nullable WidgetSprites sprites = spritesMap.get(getCurrentValue());
        Identifier spriteLocation = sprites != null
                ? sprites.get(isActive(), isHoveredOrFocused())
                : TextureManager.INTENTIONAL_MISSING_TEXTURE;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteLocation, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean handled) {
        if ((event.button() == 0 || event.button() == 1) && isMouseOver(event.x(), event.y())) {
            cycle(event.button() == 1);
            playDownSound(Minecraft.getInstance().getSoundManager());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        cycle(scrollY < 0d);
        playDownSound(Minecraft.getInstance().getSoundManager());
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        boolean pressed = super.keyPressed(event);

        if (pressed)
            cycle(Minecraft.getInstance().hasShiftDown());

        return pressed;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (clickSound != null) {
            handler.play(SimpleSoundInstance.forUI(clickSound, 1.0F));
        }
    }

    public interface OnCycle<T> {
        void onCycle(CycleButton<T> button, T newValue);
    }
}
