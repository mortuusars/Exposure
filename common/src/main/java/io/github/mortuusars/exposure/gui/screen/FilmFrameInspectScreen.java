package io.github.mortuusars.exposure.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.entity.Lightroom;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.util.GuiUtil;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilmFrameInspectScreen extends ZoomableScreen {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
    public static final ResourceLocation WIDGETS_TEXTURE = Exposure.resource("textures/gui/widgets.png");
    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;
    public static final int BUTTON_SIZE = 16;

    private final LightroomScreen lightroomScreen;
    private final LightroomMenu lightroomMenu;

    private ImageButton previousButton;
    private ImageButton nextButton;

    public FilmFrameInspectScreen(LightroomScreen lightroomScreen, LightroomMenu lightroomMenu) {
        super(Component.empty());
        this.lightroomScreen = lightroomScreen;
        this.lightroomMenu = lightroomMenu;
        zoom.minZoom = zoom.defaultZoom / (float) Math.pow(zoom.step, 2f);
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private LightroomMenu getLightroomMenu() {
        return lightroomMenu;
    }

    @Override
    protected void init() {
        super.init();

        zoomFactor = (float) height / BG_SIZE;

        previousButton = new ImageButton(0, (int) (height / 2f - BUTTON_SIZE / 2f), BUTTON_SIZE, BUTTON_SIZE,
                0, 0, BUTTON_SIZE, WIDGETS_TEXTURE, this::buttonPressed);
        nextButton = new ImageButton(width - BUTTON_SIZE, (int) (height / 2f - BUTTON_SIZE / 2f), BUTTON_SIZE, BUTTON_SIZE,
                16, 0, BUTTON_SIZE, WIDGETS_TEXTURE, this::buttonPressed);

        addRenderableWidget(previousButton);
        addRenderableWidget(nextButton);
    }

    private void buttonPressed(Button button) {
        if (button == previousButton)
            lightroomScreen.changeFrame(PagingDirection.PREVIOUS);
        else if (button == nextButton)
            lightroomScreen.changeFrame(PagingDirection.NEXT);
    }

    public void close() {
        Minecraft.getInstance().setScreen(lightroomScreen);
        if (minecraft.player != null)
            minecraft.player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, 0.7f);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        poseStack.pushPose();
        poseStack.translate(0, 0, 500); // Otherwise exposure will overlap buttons
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        super.render(poseStack, mouseX, mouseY, partialTick);
        poseStack.popPose();

        if (zoom.targetZoom == zoom.minZoom) {
            close();
            return;
        }

        poseStack.pushPose();

        poseStack.translate(x, y, 0);
        poseStack.translate(width / 2f, height / 2f, 0);
        poseStack.scale(scale, scale, scale);

        RenderSystem.setShaderTexture(0, TEXTURE);

        poseStack.translate(BG_SIZE / -2f, BG_SIZE / -2f, 0);

        GuiUtil.blit(poseStack, 0, 0, BG_SIZE, BG_SIZE, 0, 0, 256, 256, 0);

        ItemStack filmStack = lightroomMenu.getSlot(Lightroom.FILM_SLOT).getItem();
        if (!(filmStack.getItem() instanceof DevelopedFilmItem film))
            return;

        FilmType negative = film.getType();

        RenderSystem.setShaderColor(negative.filmR, negative.filmG, negative.filmB, negative.filmA);

        GuiUtil.blit(poseStack, 0, 0, BG_SIZE, BG_SIZE, 0, BG_SIZE, 256, 256, 0);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.translate(12, 12, 0);

        int currentFrame = getLightroomMenu().getSelectedFrame();
        @Nullable CompoundTag frame = getLightroomMenu().getFrameIdByIndex(currentFrame);
        if (frame != null)
            lightroomScreen.renderFrame(frame, poseStack, 0, 0, FRAME_SIZE, 1f, negative);

        poseStack.popPose();

        previousButton.visible = currentFrame != 0;
        previousButton.active = currentFrame != 0;
        nextButton.visible = currentFrame != getLightroomMenu().getTotalFrames() - 1;
        nextButton.active = currentFrame != getLightroomMenu().getTotalFrames() - 1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE || minecraft.options.keyInventory.matches(keyCode, scanCode))
            zoom.set(0f);
        else if (minecraft.options.keyLeft.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_LEFT)
            lightroomScreen.changeFrame(PagingDirection.PREVIOUS);
        else if (minecraft.options.keyRight.matches(keyCode, scanCode) || keyCode == InputConstants.KEY_RIGHT)
            lightroomScreen.changeFrame(PagingDirection.NEXT);
        else
            return super.keyPressed(keyCode, scanCode, modifiers);

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (handled)
            return true;

        if (button == 1) { // Right Click
            zoom.set(0f);
            return true;
        }

        return false;
    }
}
