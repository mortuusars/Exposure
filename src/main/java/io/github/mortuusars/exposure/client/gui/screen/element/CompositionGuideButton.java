package io.github.mortuusars.exposure.client.gui.screen.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.infrastructure.SynchronizedCameraInHandActions;
import io.github.mortuusars.exposure.camera.infrastructure.CompositionGuide;
import io.github.mortuusars.exposure.camera.infrastructure.CompositionGuides;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CompositionGuideButton extends CycleButton {
    private final List<CompositionGuide> guides;

    public CompositionGuideButton(Screen screen, int x, int y, int width, int height, int u, int v,  ResourceLocation texture) {
        super(screen, x, y, width, height, u, v, height, texture);
        guides = CompositionGuides.getGuides();


        CameraInHand camera = CameraInHand.ofPlayer(Minecraft.getInstance().player);
        CompositionGuide guide = camera.getItem().getCompositionGuide(camera.getStack());

        int currentGuideIndex = 0;

        for (int i = 0; i < guides.size(); i++) {
            if (guides.get(i).getId().equals(guide.getId())) {
                currentGuideIndex = i;
                break;
            }
        }

        setupButtonElements(guides.size(), currentGuideIndex);
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
        RenderSystem.setShaderTexture(0, Exposure.resource("textures/gui/viewfinder/icon/composition_guide/" + guides.get(index).getId() + ".png"));
        blit(poseStack, x, y + 4, 0, 0, 0, 15, 14, 15, 14);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        screen.renderTooltip(poseStack, List.of(Component.translatable("gui.exposure.viewfinder.composition_guide.tooltip"),
                ((MutableComponent) getMessage()).withStyle(ChatFormatting.GRAY)), Optional.empty(), mouseX, mouseY);
    }

    @Override
    public @NotNull Component getMessage() {
        return guides.get(index).translate();
    }

    @Override
    protected void onCycle() {
        CameraInHand camera = CameraInHand.ofPlayer(Minecraft.getInstance().player);
        if (!camera.isEmpty())
            SynchronizedCameraInHandActions.setCompositionGuide(guides.get(index));
    }
}
