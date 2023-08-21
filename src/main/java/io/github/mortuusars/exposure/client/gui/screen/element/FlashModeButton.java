package io.github.mortuusars.exposure.client.gui.screen.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.camera.component.FlashMode;
import io.github.mortuusars.exposure.camera.infrastructure.SynchronizedCameraInHandActions;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FlashModeButton extends CycleButton {

    private final List<FlashMode> flashModes;

    public FlashModeButton(Screen screen, int x, int y, int width, int height, int u, int v, ResourceLocation texture) {
        super(screen, x, y, width, height, u, v, height, texture);
        flashModes = Arrays.stream(FlashMode.values()).toList();

        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        FlashMode guide = camera.getItem().getFlashMode(camera.getStack());

        int currentGuideIndex = 0;

        for (int i = 0; i < flashModes.size(); i++) {
            if (flashModes.get(i).getId().equals(guide.getId())) {
                currentGuideIndex = i;
                break;
            }
        }

        setupButtonElements(flashModes.size(), currentGuideIndex);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_BUTTON_CLICK.get(),
                Objects.requireNonNull(Minecraft.getInstance().level).random.nextFloat() * 0.15f + 0.93f, 0.7f));
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.renderButton(poseStack, mouseX, mouseY, partialTick);

        // Icon
        RenderSystem.setShaderTexture(0, Exposure.resource("textures/gui/viewfinder/icon/flash_mode/" + flashModes.get(index).getId() + ".png"));
        blit(poseStack, x, y + 4, 0, 0, 0, 15, 14, 15, 14);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        screen.renderTooltip(poseStack, List.of(Component.translatable("gui.exposure.viewfinder.flash_mode.tooltip"),
                ((MutableComponent) getMessage()).withStyle(ChatFormatting.GRAY)), Optional.empty(), mouseX, mouseY);
    }

    @Override
    public @NotNull Component getMessage() {
        return flashModes.get(index).translate();
    }

    @Override
    protected void onCycle() {
        CameraInHand camera = Exposure.getCamera().getCameraInHand(Minecraft.getInstance().player);
        if (!camera.isEmpty())
            SynchronizedCameraInHandActions.setFlashMode(flashModes.get(index));
    }
}
