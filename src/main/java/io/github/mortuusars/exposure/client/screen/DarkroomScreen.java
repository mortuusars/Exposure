package io.github.mortuusars.exposure.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.block.entity.DarkroomBlockEntity;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import io.github.mortuusars.exposure.client.render.PhotographRenderer;
import io.github.mortuusars.exposure.item.FilmItem;
import io.github.mortuusars.exposure.menu.DarkroomMenu;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class DarkroomScreen extends AbstractContainerScreen<DarkroomMenu> {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/darkroom.png");

    public DarkroomScreen(DarkroomMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        ExposureStorage.CLIENT.clear();
    }

    @Override
    protected void init() {
        imageWidth = 176;
        imageHeight = 210;
        super.init();
        inventoryLabelY = 116;

        addRenderableWidget(new Button( leftPos + 100, topPos + 94, 49, 20, Component.literal("PRINT"), pButton ->
                Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, DarkroomMenu.PRINT_BUTTON_ID)));

//        this.manualDeliveryButton = new ImageButton(getGuiLeft() + 74, getGuiTop() + 36, 28, 20,
//                176, 70, 20, TEXTURE, 256, 256,
//                this::manualDeliveryButtonPressed,
//                (button, poseStack, mouseX, mouseY) -> renderTooltip(poseStack, manualDeliveryButtonTooltip, Optional.empty(), mouseX, mouseY),
//                this.manualDeliveryButtonTitle);

//        addRenderableWidget(manualDeliveryButton);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        renderFilmFrame(poseStack, partialTick, mouseX, mouseY);

        // ARROW
//        float progress = menu.getDeliveryProgress();
//        int arrowWidth = 22;
//        int arrowHeight = 16;
//        int progressInPixels = Mth.clamp((int)((arrowWidth + 1) * progress), 0, arrowWidth);
//        this.blit(poseStack, leftPos + 77, topPos + 37, 176, 0, progressInPixels, arrowHeight);
    }

    protected void renderFilmFrame(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        Slot filmSlot = menu.slots.get(DarkroomBlockEntity.FILM_SLOT);
        if (filmSlot.hasItem() && filmSlot.getItem().getItem() instanceof FilmItem filmItem) {
            List<ExposureFrame> exposedFrames = filmItem.getExposedFrames(filmSlot.getItem());
            if (exposedFrames.size() > 0) {
                int currentFrameId = menu.getData().get(DarkroomBlockEntity.CONTAINER_DATA_CURRENT_FRAME_ID);
                if (currentFrameId >= exposedFrames.size())
                    return;

                ExposureFrame exposureFrame = exposedFrames.get(currentFrameId);
                Exposure.getStorage().getOrQuery(exposureFrame.id).ifPresent(exposureData -> {
                    int size = PhotographRenderer.SIZE;

                    poseStack.pushPose();
                    poseStack.translate(this.leftPos + 64, this.topPos + 31, 0);
                    float scale = 48f / size;
                    poseStack.scale(scale, scale, scale);
                    ExposureClient.getExposureRenderer().render(exposureFrame.id, exposureData, true, poseStack, size, size);
                    poseStack.popPose();
                });
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_LEFT)
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, DarkroomMenu.NEXT_FRAME_BUTTON_ID);
        else if (keyCode == InputConstants.KEY_RIGHT)
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, DarkroomMenu.PREVIOUS_FRAME_BUTTON_ID);

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
