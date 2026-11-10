package io.github.mortuusars.exposure.client.gui.screen.test;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class Slider extends AbstractWidget {
    public static final ResourceLocation SLIDER_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider");
    public static final ResourceLocation HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_highlighted");
    public static final ResourceLocation SLIDER_HANDLE_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle");
    public static final ResourceLocation SLIDER_HANDLE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle_highlighted");

    protected static final int TEXT_MARGIN = 2;
    protected static final int HANDLE_WIDTH = 8;
    protected static final int HANDLE_HALF_WIDTH = 4;

    protected double position;
    protected boolean canChangeValue;

    protected final double defaultValue;
    protected final double min;
    protected final double max;
    protected final DecimalFormat displayFormat;
    protected final String name;
    protected final Consumer<Double> onChanged;

    @Nullable
    protected Pair<Integer, Integer> horizontalGradient;

    public Slider(int x, int y, int width, int height,
                  double defaultValue, double min, double max, int displayedPrecision, String name, Consumer<Double> onChanged) {
        this(x, y, width, height, CommonComponents.EMPTY, defaultValue, min, max, displayedPrecision, name, onChanged);
    }

    public Slider(int x, int y, int width, int height, Component message,
                  double defaultValue, double min, double max, int displayedPrecision, String name, Consumer<Double> onChanged) {
        super(x, y, width, height, message);
        this.position = (defaultValue - min) / (max - min);
        this.defaultValue = Mth.clamp(defaultValue, min, max);
        this.min = min;
        this.max = max;
        String zeros = "#".repeat(displayedPrecision);
        this.displayFormat = new DecimalFormat("#" + (zeros.isEmpty() ? "" : "." + zeros));
        this.name = name;
        this.onChanged = onChanged;
        updateMessage();
    }

    public Slider setHorizontalGradient(int leftColor, int rightColor) {
        this.horizontalGradient = Pair.of(leftColor, rightColor);
        return this;
    }

    public void removeHorizontalGradient() {
        this.horizontalGradient = null;
    }

    protected ResourceLocation getSprite() {
        return this.isFocused() && !this.canChangeValue ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE;
    }

    protected ResourceLocation getHandleSprite() {
        return !this.isHovered && !this.canChangeValue ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
    }

    // -- Value

    public double getPosition() {
        return this.position;
    }

    public void setPosition(double position) {
        double d = this.position;
        this.position = Mth.clamp(position, 0.0, 1.0);
        if (d != this.position) {
            this.applyValue();
        }

        this.updateMessage();
    }

    public Double getValue() {
        return Mth.clampedLerp(min, max, position);
    }

    public void setValue(double value) {
        double positionFromValue = (value - min) / (max - min);
        setPosition(positionFromValue);
    }

    public void resetToDefault() {
        setValue(defaultValue);
    }

    protected void updateMessage() {
        setMessage(Component.literal(name + ": " + displayFormat.format(getValue())));
    }

    protected void applyValue() {
        onChanged.accept(getValue());
    }

    // -- Input

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setPositionFromMouse(mouseX);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.canChangeValue = false;
        } else {
            InputType inputType = Minecraft.getInstance().getLastInputType();
            if (inputType == InputType.MOUSE || inputType == InputType.KEYBOARD_TAB) {
                this.canChangeValue = true;
            }
        }
    }

    protected void setPositionFromMouse(double mouseX) {
        this.setPosition((mouseX - (double)(this.getX() + HANDLE_HALF_WIDTH)) / (double)(this.width - HANDLE_WIDTH));
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_RIGHT && active && visible && clicked(mouseX, mouseY)) {
            resetToDefault();
            playDownSound(Minecrft.get().getSoundManager());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        this.setPositionFromMouse(mouseX);
        super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (CommonInputs.selected(keyCode)) {
            this.canChangeValue = !this.canChangeValue;
            return true;
        } else {
            if (this.canChangeValue) {
                boolean bl = keyCode == 263;
                if (bl || keyCode == 262) {
                    float f = bl ? -1.0F : 1.0F;
                    this.setPosition(position + (double)(f / (float)(width - HANDLE_WIDTH)));
                    return true;
                }
            }

            return false;
        }
    }

    // -- Render

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        guiGraphics.blitSprite(getSprite(), getX(), getY(), getWidth(), getHeight());

        if (active && horizontalGradient != null) {
            fillHorizontalGradient(guiGraphics, getX() + 1, getY() + 1, getX() + getWidth() - 1,
                    getY() + getHeight() - 1, horizontalGradient.getFirst(), horizontalGradient.getSecond());
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 50);

        guiGraphics.blitSprite(getHandleSprite(), getX() + (int)(position * (double)(width - HANDLE_WIDTH)), getY(), HANDLE_WIDTH, getHeight());
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int textColor = (active ? 0xFFFFFF : 0xA0A0A0) | Mth.ceil(alpha * 255.0F) << 24;
        renderScrollingString(guiGraphics, minecraft.font, TEXT_MARGIN, textColor);
        guiGraphics.pose().popPose();
    }

    private void fillHorizontalGradient(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        consumer.addVertex(matrix4f, (float)x1, (float)y1, 0).setColor(colorFrom);
        consumer.addVertex(matrix4f, (float)x1, (float)y2, 0).setColor(colorFrom);
        consumer.addVertex(matrix4f, (float)x2, (float)y2, 0).setColor(colorTo);
        consumer.addVertex(matrix4f, (float)x2, (float)y1, 0).setColor(colorTo);
    }

    // --

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.slider", this.getMessage());
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
            }
        }
    }
}
