package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class CameraAttachmentsScreen extends AbstractContainerScreen<CameraAttachmentsMenu> {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/camera_attachments.png");

    public CameraAttachmentsScreen(CameraAttachmentsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        this.imageHeight = 185;
        inventoryLabelY = this.imageHeight - 94;
        super.init();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        Slot filmSlot = menu.slots.get(CameraItem.FILM_ATTACHMENT.slot());
        if (!filmSlot.hasItem())
            this.blit(poseStack, leftPos + filmSlot.x - 1, topPos + filmSlot.y - 1, 238, 0, 18, 18);

        Slot lensSlot = menu.slots.get(CameraItem.LENS_ATTACHMENT.slot());
        boolean hasLens = lensSlot.hasItem();
        if (hasLens)
            this.blit(poseStack, leftPos + 96, topPos + 42, 176, 0, 49, 49);
        else
            this.blit(poseStack, leftPos + lensSlot.x - 1, topPos + lensSlot.y - 1, 238, 18, 18, 18);

        Slot filterSlot = menu.slots.get(CameraItem.FILTER_ATTACHMENT.slot());
        if (filterSlot.hasItem()) {
            int x = hasLens ? 111 : 99;
            int y = hasLens ? 48 : 42;
            this.blit(poseStack, leftPos + x , topPos + y, 176, 50, 28, 44);

            float r = 1f;
            float g = 1f;
            float b = 1f;

            ResourceLocation key = ForgeRegistries.ITEMS.getKey(filterSlot.getItem().getItem());
            if (key != null) {
                if (key.getNamespace().equals("minecraft") && key.getPath().contains("_stained_glass_pane")) {
                    String colorString = key.getPath().replace("_stained_glass_pane", "");
                    DyeColor color = DyeColor.byName(colorString, DyeColor.WHITE);
                    int rgb = color.getFireworkColor();
                    r = Mth.clamp(((rgb >> 16) & 0xFF) / 255f, 0f, 1f);
                    g = Mth.clamp((((rgb >> 8) & 0xFF) / 255f), 0f, 1f);
                    b = Mth.clamp((rgb  & 0xFF) / 255f, 0f, 1f);
                }
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(r, g, b, 1f);
            this.blit(poseStack, leftPos + x, topPos + y, 205, 50, 28, 44);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
        else
            this.blit(poseStack, leftPos + filterSlot.x - 1, topPos + filterSlot.y - 1, 238, 36, 18, 18);
    }
}
