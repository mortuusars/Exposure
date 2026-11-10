package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.component.SteppedZoom;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.FilmColor;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilmFrameInspectScreen extends Screen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");

    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;

    protected final Pager pager = new Pager()
            .setChangeSound(new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK))
            .onPageChanged(this::pageChanged);

    protected final SteppedZoom zoom = new SteppedZoom()
            .zoomInSteps(4)
            .zoomOutSteps(4)
            .zoomPerStep(1.4)
            .defaultZoom(1);

    protected final KeyBindings keyBindings = KeyBindings.of(
            Key.press(Minecrft.options().keyInventory).executes(this::onClose),
            Key.press(InputConstants.KEY_ADD).or(Key.press(InputConstants.KEY_EQUALS)).executes(zoom::zoomIn),
            Key.press(GLFW.GLFW_KEY_KP_SUBTRACT).or(Key.press(InputConstants.KEY_MINUS)).executes(zoom::zoomOut),
            Key.press(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::previousPage),
            Key.press(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::nextPage),
            Key.release(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::resetCooldown),
            Key.release(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::resetCooldown)
    );

    protected final List<Frame> frames;

    protected float zoomFactor;
    protected float x;
    protected float y;

    public FilmFrameInspectScreen(List<Frame> frames) {
        this(frames, frames.size() - 1);
    }

    public FilmFrameInspectScreen(List<Frame> frames, int startingFrame) {
        super(Component.empty());
        this.frames = new ArrayList<>(frames);
        initPager(startingFrame);
    }

    protected void initPager(int startingFrame) {
        pager.setPagesCount(frames.size());
        pager.setPage(startingFrame);
        Collections.rotate(frames, -startingFrame);
    }

    @Override
    protected void init() {
        super.init();

        zoomFactor = ((float) height / BG_SIZE) / (float)zoom.getZoomPerStep();

        ImageButton previousButton = new ImageButton(0, (int) (height / 2f - 16 / 2f), 16, 16,
                Widgets.PREVIOUS_BUTTON_SPRITES,
                button -> pager.changePage(PagingDirection.PREVIOUS), Component.translatable("gui.exposure.previous_page"));
        addRenderableWidget(previousButton);

        ImageButton nextButton = new ImageButton(width - 16, (int) (height / 2f - 16 / 2f), 16, 16,
                Widgets.NEXT_BUTTON_SPRITES,
                button -> pager.changePage(PagingDirection.NEXT), Component.translatable("gui.exposure.next_page"));
        addRenderableWidget(nextButton);

        pager.setPagesCount(frames.size())
                .setPreviousPageButton(previousButton)
                .setNextPageButton(nextButton);
    }

    protected Frame getCurrentFrame() {
        return frames.getFirst();
    }

    protected void pageChanged(int oldPage, int newPage) {
        int distance = newPage - oldPage;
        Collections.rotate(frames, -distance);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float scale = (float) (zoom.get() * zoomFactor);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        renderTransparentBackground(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().translate(width / 2f, height / 2f, 50);
        guiGraphics.pose().scale(scale, scale, scale);

        guiGraphics.pose().translate(BG_SIZE / -2f, BG_SIZE / -2f, 0);

        RenderSystem.setShaderTexture(0, TEXTURE);
        GuiUtil.blit(guiGraphics.pose(), 0, 0, BG_SIZE, BG_SIZE, 0, 0, 256, 256, 0);

        Frame frame = getCurrentFrame();
        ExposureType filmType = frame.type();
        FilmColor filmColor = filmType.getFilmColor();

        RenderSystem.setShaderColor(filmColor.r(), filmColor.g(), filmColor.b(), filmColor.a());
        GuiUtil.blit(guiGraphics.pose(), 0, 0, BG_SIZE, BG_SIZE, 0, BG_SIZE, 256, 256, 0);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.pose().translate(12, 12, 0);
        RenderableImage image = ExposureClient.renderedExposures().getOrCreate(frame).modifyWith(ImageEffect.NEGATIVE_FILM);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        ExposureClient.imageRenderer().render(image,  guiGraphics.pose(), bufferSource,
                new RenderCoordinates(0, 0, FRAME_SIZE, FRAME_SIZE), filmType.getImageColor());
        bufferSource.endBatch();

        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        // Places widgets above, because they will be covered when photo is zoomed in
//        guiGraphics.pose().translate(0, 0, 100);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Background is rendered manually in #render method.
        // Otherwise, background will be rendered on top
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyBindings.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return keyBindings.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;

        if (scrollY >= 0.0) {
            zoom.zoomIn();
        } else {
            zoom.zoomOut();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            float centerX = width / 2f;
            float centerY = height / 2f;
            x = (float) Mth.clamp(x + dragX, -centerX, centerX);
            y = (float) Mth.clamp(y + dragY, -centerY, centerY);
            return true;
        }

        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
