package io.github.mortuusars.exposure.client.gui.screen.test;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.mortaar.util.color.Color;
import io.github.mortuusars.exposure.world.camera.film.properties.Levels;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class TestImageScreen extends Screen {
    protected float scale = 1f;

    protected boolean isCapturing;
    @Nullable
    protected Image image;
    @Nullable
    protected RenderableImage renderableImage;

    protected List<AbstractWidget> rightPaneWidgets = new ArrayList<>();

    protected Slider sizeSlider;

    protected ShutterSpeedSlider shutterSpeedSlider;
    protected Slider exposureSlider;
    protected Slider contrastSlider;

    protected Slider shadowsSlider;
    protected Slider midtonesSlider;
    protected Slider highlightsSlider;
    protected Slider blackSlider;
    protected Slider whiteSlider;

    protected Slider balanceRedSlider;
    protected Slider balanceGreenSlider;
    protected Slider balanceBlueSlider;

    protected Slider hueSlider;
    protected Slider saturationSlider;
    protected Slider brightnessSlider;

    protected Slider noiseSlider;
    protected Checkbox bw;
    protected Checkbox aged;

    protected float rightPaneScroll = 0f;
    protected long applyEditsAt = -1;

    public TestImageScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        int sliderWidth = 120;
        int sliderHeight = 15;
        int spacingInGroup = 1;
        int spacingBetweenGroups = 4;
        int screenEdgeMargin = 5;
        int labelXOffset = 3;

        int x = width - sliderWidth - screenEdgeMargin;
        int labelX = x + labelXOffset;
        int y = screenEdgeMargin;

        rightPaneWidgets.clear();

        sizeSlider = new Slider(x, y, sliderWidth, sliderHeight,
                320, 1, 2048, 0, "Size", v -> onChanged());
        addRenderableWidget(sizeSlider);
        rightPaneWidgets.add(sizeSlider);
        y += sizeSlider.getHeight();
        y += spacingBetweenGroups;


        MultiLineTextWidget toneLabel = new MultiLineTextWidget(labelX, y, Component.literal("Tone"), font);
        addRenderableOnly(toneLabel);
        rightPaneWidgets.add(toneLabel);
        y += font.lineHeight;

        shutterSpeedSlider = new ShutterSpeedSlider(x, y, sliderWidth, sliderHeight, "Shutter Speed",
                Exposure.Items.CAMERA.get().getAvailableShutterSpeeds(), ShutterSpeed.DEFAULT, v -> onChanged());
        shutterSpeedSlider.setHorizontalGradient(0x44000000, 0x44FFFFFF);
        addRenderableWidget(shutterSpeedSlider);
        rightPaneWidgets.add(shutterSpeedSlider);
        y += shutterSpeedSlider.getHeight();
        y += spacingInGroup;

        exposureSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, -4, 4, 2, "Sensitivity", v -> onChanged());
        exposureSlider.setHorizontalGradient(0x44000000, 0x44FFFFFF);
        addRenderableWidget(exposureSlider);
        rightPaneWidgets.add(exposureSlider);
        y += exposureSlider.getHeight();
        y += spacingInGroup;

        contrastSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, -1, 1, 2, "Contrast", v -> onChanged());
        contrastSlider.setHorizontalGradient(0x44FFFFFF, 0x44000000);
        addRenderableWidget(contrastSlider);
        rightPaneWidgets.add(contrastSlider);
        y += contrastSlider.getHeight();
        y += spacingBetweenGroups;


        MultiLineTextWidget levelsLabel = new MultiLineTextWidget(labelX, y, Component.literal("Levels"), font);
        addRenderableOnly(levelsLabel);
        rightPaneWidgets.add(levelsLabel);
        y += font.lineHeight;

        shadowsSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, 0, 255, 0, "Shadows", v -> onChanged());
        shadowsSlider.setHorizontalGradient(0x44FFFFFF, 0x44000000);
        addRenderableWidget(shadowsSlider);
        rightPaneWidgets.add(shadowsSlider);
        y += shadowsSlider.getHeight();
        y += spacingInGroup;

        midtonesSlider = new Slider(x, y, sliderWidth, sliderHeight,
                128, 0, 255, 0, "Midtones", v -> onChanged());
        midtonesSlider.setHorizontalGradient(0x44FFFFFF, 0x44000000);
        addRenderableWidget(midtonesSlider);
        rightPaneWidgets.add(midtonesSlider);
        y += shadowsSlider.getHeight();
        y += spacingInGroup;

        highlightsSlider = new Slider(x, y, sliderWidth, sliderHeight,
                255, 0, 255, 0, "Highlights", v -> onChanged());
        highlightsSlider.setHorizontalGradient(0x44FFFFFF, 0x44000000);
        addRenderableWidget(highlightsSlider);
        rightPaneWidgets.add(highlightsSlider);
        y += shadowsSlider.getHeight();
        y += spacingInGroup;

        blackSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, 0, 255, 0, "Black", v -> onChanged());
        blackSlider.setHorizontalGradient(0x44000000, 0x44FFFFFF);
        addRenderableWidget(blackSlider);
        rightPaneWidgets.add(blackSlider);
        y += shadowsSlider.getHeight();
        y += spacingInGroup;

        whiteSlider = new Slider(x, y, sliderWidth, sliderHeight,
                255, 0, 255, 0, "White", v -> onChanged());
        whiteSlider.setHorizontalGradient(0x44000000, 0x44FFFFFF);
        addRenderableWidget(whiteSlider);
        rightPaneWidgets.add(whiteSlider);
        y += shadowsSlider.getHeight();
        y += spacingBetweenGroups;


        MultiLineTextWidget hsbLabel = new MultiLineTextWidget(labelX, y, Component.literal("HSB"), font);
        addRenderableOnly(hsbLabel);
        rightPaneWidgets.add(hsbLabel);
        y += font.lineHeight;

        hueSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, -1, 1, 2, "Hue", v -> onChanged());
        hueSlider.setHorizontalGradient(0x44FF0000, 0x44FF00FF);
        addRenderableWidget(hueSlider);
        rightPaneWidgets.add(hueSlider);
        y += shadowsSlider.getHeight();
        y += spacingInGroup;

        saturationSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, -1, 1, 2, "Saturation", v -> onChanged());
        saturationSlider.setHorizontalGradient(0x44777777, 0x44FF2211);
        addRenderableWidget(saturationSlider);
        rightPaneWidgets.add(saturationSlider);
        y += shadowsSlider.getHeight();
        y += spacingInGroup;

        brightnessSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, -1, 1, 2, "Brightness", v -> onChanged());
        brightnessSlider.setHorizontalGradient(0x44000000, 0x44FFFFFF);
        addRenderableWidget(brightnessSlider);
        rightPaneWidgets.add(brightnessSlider);
        y += shadowsSlider.getHeight();
        y += spacingBetweenGroups;

        MultiLineTextWidget colorBalanceLabel = new MultiLineTextWidget(labelX, y, Component.literal("Color Balance"), font);
        addRenderableOnly(colorBalanceLabel);
        rightPaneWidgets.add(colorBalanceLabel);
        y += font.lineHeight;

        balanceRedSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, -1, 1, 2, "Red", v -> onChanged());
        balanceRedSlider.setHorizontalGradient(0x4400FFFF, 0x44FF0000);
        addRenderableWidget(balanceRedSlider);
        rightPaneWidgets.add(balanceRedSlider);
        y += shadowsSlider.getHeight();
        y += spacingInGroup;

        balanceGreenSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, -1, 1, 2, "Green", v -> onChanged());
        balanceGreenSlider.setHorizontalGradient(0x44FF00FF, 0x4400FF00);
        addRenderableWidget(balanceGreenSlider);
        rightPaneWidgets.add(balanceGreenSlider);
        y += shadowsSlider.getHeight();
        y += spacingInGroup;

        balanceBlueSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, -1, 1, 2, "Blue", v -> onChanged());
        balanceBlueSlider.setHorizontalGradient(0x44FFFF00, 0x440000FF);
        addRenderableWidget(balanceBlueSlider);
        rightPaneWidgets.add(balanceBlueSlider);
        y += shadowsSlider.getHeight();
        y += spacingBetweenGroups;


        MultiLineTextWidget effectsLabel = new MultiLineTextWidget(labelX, y, Component.literal("Effects"), font);
        addRenderableOnly(effectsLabel);
        rightPaneWidgets.add(effectsLabel);
        y += font.lineHeight;

        noiseSlider = new Slider(x, y, sliderWidth, sliderHeight,
                0, 0, 1, 2, "Noise", v -> onChanged());
        noiseSlider.setHorizontalGradient(0x44777777, 0x44FFFFFF);
        addRenderableWidget(noiseSlider);
        rightPaneWidgets.add(noiseSlider);
        y += shadowsSlider.getHeight();
        y += spacingBetweenGroups;

        bw = Checkbox.builder(Component.literal("BW"), font)
                .pos(x, y)
                .onValueChange((checkbox, bl) -> onChanged())
                .build();
        addRenderableWidget(bw);
        rightPaneWidgets.add(bw);

        aged = Checkbox.builder(Component.literal("AGED"), font)
                .pos(x + bw.getWidth() + Button.DEFAULT_SPACING, y)
                .onValueChange((checkbox, bl) -> onChanged())
                .build();
        addRenderableWidget(aged);
        rightPaneWidgets.add(aged);


        Button capture = Button.builder(Component.literal("CAPTURE"), b -> capture())
                .size(60, Button.DEFAULT_HEIGHT)
                .pos(screenEdgeMargin, height - Button.DEFAULT_HEIGHT - screenEdgeMargin)
                .build();
        addRenderableWidget(capture);

        if (image == null) {
            capture();
        }
    }

    protected void capture() {
        if (isCapturing) {
            Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 1f));
            Minecrft.playerOrThrow().displayClientMessage(Component.literal("Capture is in progress."), false);
            return;
        }
        isCapturing = true;

        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.SHUTTER_OPEN.get(), 1f));

        ExposureClient.cycles().enqueueTask(Capture.of(Capture.screenshot(), CaptureAction.hideGui())
                .handleErrorAndGetResult(err -> Minecrft.execute(() ->
                        Minecrft.playerOrThrow().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false)))
                .thenAsync(ImageEffect.Crop.SQUARE_CENTER::modify)
                .onError(err -> Minecrft.execute(() ->
                        Minecrft.playerOrThrow().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false)))
                .accept(this::setImage));
    }

    protected void applyEdits() {
        if (renderableImage == null) {
            Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 1f));
            Minecrft.playerOrThrow().displayClientMessage(Component.literal("No image to modify."), false);
            return;
        }

        try {
            renderableImage = RenderableImage.of("test_image", image)
                    .modifyWith(ImageEffect.Resize.to(sizeSlider.getValue().intValue()))
                    .modifyWith(ImageEffect.exposure(shutterSpeedSlider.getShutterSpeed().getBrightness() * (exposureSlider.getValue().floatValue() + 1f)))
                    .modifyWith(ImageEffect.contrast(contrastSlider.getValue().floatValue()))
                    .modifyWith(ImageEffect.levels(new Levels(
                            shadowsSlider.getValue().intValue(),
                            midtonesSlider.getValue().intValue(),
                            highlightsSlider.getValue().intValue(),
                            blackSlider.getValue().intValue(),
                            whiteSlider.getValue().intValue())))
                    .modifyWith(ImageEffect.hsb(
                            hueSlider.getValue().floatValue(),
                            saturationSlider.getValue().floatValue(),
                            brightnessSlider.getValue().floatValue()))
                    .modifyWith(ImageEffect.colorBalance(
                            balanceRedSlider.getValue().floatValue(),
                            balanceGreenSlider.getValue().floatValue(),
                            balanceBlueSlider.getValue().floatValue()))
                    .modifyWith(ImageEffect.noise(noiseSlider.getValue().floatValue()))
                    .modifyWith(ImageEffect.optional(bw.selected(), ImageEffect.BLACK_AND_WHITE))
                    .modifyWith(image -> Palettizer.DITHERED.palettize(image,
                            ColorPalettes.getDefault(Minecrft.registryAccess()).value()), "palette")
                    .modifyWith(ImageEffect.optional(aged.selected(), ImageEffect.AGED));
            ExposureClient.imageRenderer().clearCacheOf("test_image");
        } catch (Exception e) {
            Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 1f));
            Minecrft.playerOrThrow().displayClientMessage(Component.literal("Failed to apply edits. " + e.getMessage()), false);
            Exposure.LOGGER.error("Failed to apply edits: ", e);
        }
        applyEditsAt = -1;
    }

    protected void onChanged() {
        applyEditsAt = UnixTimestamp.Milliseconds.now()/* + 10*/;
    }

    private void setImage(Image image) {
        if (this.image != null) this.image.close();
        if (this.renderableImage != null) this.renderableImage.close();

        this.image = image;
        this.renderableImage = RenderableImage.of("test_image", image);
        applyEdits();
        ExposureClient.imageRenderer().clearCacheOf("test_image");
        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.SHUTTER_CLOSE.get(), 1f));
        isCapturing = false;
    }

    // --

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        fillHorizontalGradient(guiGraphics, width - 100, -500, width, height + 500, 0x00000000, 0xBB111111);

        if (isCapturing) {
            String txt = "Capturing...";
            guiGraphics.drawString(font, txt, width / 2 - font.width(txt) / 2, height / 2 - 4, 0xFFFFFFFF);
            return;
        }

        if (renderableImage == null) {
            String txt = "<No Image>";
            guiGraphics.drawString(font, txt, width / 2 - font.width(txt) / 2, height / 2 - 4, 0xFFFFFFFF);
            return;
        }

        if (applyEditsAt > 0 && UnixTimestamp.Milliseconds.now() >= applyEditsAt) {
            applyEdits();
        }

        guiGraphics.pose().pushPose();
        float size = height * 0.8f * scale;
        guiGraphics.pose().translate(width / 2f - size / 2f, height / 2f - size / 2f, -100);

        float borderPercent = 0.02f;
        guiGraphics.fill(Mth.floor(-size * borderPercent), Mth.floor(-size * borderPercent),
                Mth.ceil(size + (size * borderPercent)), Mth.ceil(size + (size * borderPercent)), 0xFFFFFFFF);
        guiGraphics.pose().scale(size, size, size);


        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        ExposureClient.imageRenderer().render(renderableImage, guiGraphics.pose(), bufferSource, RenderCoordinates.DEFAULT, Color.WHITE);
        bufferSource.endBatch();
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }

        int value = Mth.sign(scrollY);

        if (mouseX > width - 100 && !rightPaneWidgets.isEmpty()) {
            int yChange = value * 14;

            if (yChange > 0 && rightPaneWidgets.getFirst().getY() >= 5) {
                return true;
            }
            if (yChange < 0 && rightPaneWidgets.getLast().getY() + rightPaneWidgets.getLast().getHeight() <= height - 5) {
                return true;
            }

            for (AbstractWidget widget : rightPaneWidgets) {
                widget.setY(widget.getY() + yChange);
                if (widget.getY() < 5 || widget.getY() + widget.getHeight() > height - 5) {
                    widget.visible = false;
                } else {
                    widget.visible = true;
                }
            }

            return true;
        }

        scale = Mth.clamp(scale + 0.2f * value, 0.2f, 2f);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;

        if (button != InputConstants.MOUSE_BUTTON_RIGHT) return false;

        // Dragging the view with mouse:
        double fov = Minecrft.options().fov().get();
        int windowWidth = Minecrft.get().getWindow().getHeight();
        int windowHeight = Minecrft.get().getWindow().getHeight();
        double xRot = (dragY / windowHeight) * fov;
        double yRot = (dragX / windowWidth) * (fov * ((double) windowHeight / windowWidth));
        Minecrft.playerOrThrow().turn(-yRot * 20, -xRot * 20);

        return true;
    }

    private void fillHorizontalGradient(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        consumer.addVertex(matrix4f, (float) x1, (float) y1, 0).setColor(colorFrom);
        consumer.addVertex(matrix4f, (float) x1, (float) y2, 0).setColor(colorFrom);
        consumer.addVertex(matrix4f, (float) x2, (float) y2, 0).setColor(colorTo);
        consumer.addVertex(matrix4f, (float) x2, (float) y1, 0).setColor(colorTo);
    }
}
