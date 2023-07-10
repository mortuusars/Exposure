package io.github.mortuusars.exposure.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.block.entity.DarkroomBlockEntity;
import io.github.mortuusars.exposure.camera.ExposureFrame;
import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import io.github.mortuusars.exposure.item.FilmItem;
import io.github.mortuusars.exposure.menu.DarkroomMenu;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
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

        Slot filmSlot = menu.slots.get(DarkroomBlockEntity.FILM_SLOT);
        if (filmSlot.hasItem() && filmSlot.getItem().getItem() instanceof FilmItem filmItem) {
            List<ExposureFrame> exposedFrames = filmItem.getExposedFrames(filmSlot.getItem());
            if (exposedFrames.size() > 0) {
                ExposureFrame exposureFrame = exposedFrames.get(0);
                Optional<ExposureSavedData> exp = Exposure.getStorage().getOrQuery(exposureFrame.id);
                if (exp.isPresent()) {
                    ExposureSavedData exposureSavedData = exp.get();
                    MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

                    poseStack.pushPose();
                    poseStack.translate(this.leftPos + 64, this.topPos + 31, 0);
                    float scale = 48f / exposureSavedData.getHeight();
                    poseStack.scale(scale, scale, scale);
                    ExposureRenderer.renderNegative(poseStack, bufferSource,
                            exposureFrame.id, exposureSavedData, LightTexture.FULL_BRIGHT);
                    poseStack.popPose();

                    bufferSource.endBatch();
                }
            }
        }

        // ARROW
//        float progress = menu.getDeliveryProgress();
//        int arrowWidth = 22;
//        int arrowHeight = 16;
//        int progressInPixels = Mth.clamp((int)((arrowWidth + 1) * progress), 0, arrowWidth);
//        this.blit(poseStack, leftPos + 77, topPos + 37, 176, 0, progressInPixels, arrowHeight);
    }
}
