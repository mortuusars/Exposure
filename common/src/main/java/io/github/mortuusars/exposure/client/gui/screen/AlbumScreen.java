package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.menu.AlbumMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AlbumScreen extends AbstractContainerScreen<AlbumMenu> {
    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/album.png");
    public static final int MAIN_FONT_COLOR = 0xB59774;
    public static final int SECONDARY_FONT_COLOR = 0xEFE4CA;

    public AlbumScreen(AlbumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void added() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 1f));
    }

    @Override
    protected void init() {
        this.imageWidth = 299;
        this.imageHeight = 188;
        super.init();

        titleLabelY = -999;
        inventoryLabelY = -999;

//        new ImageButton(25, 21, 108, 108, )
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 0,
                this.imageWidth, this.imageHeight, 512, 512);

        List<AlbumItem.AlbumPage> pages = getMenu().getPages();

        if (pages.size() >= 1) {
            AlbumItem.AlbumPage page = pages.get(0);
            ItemStack photoStack = page.photo();

            guiGraphics.blit(TEXTURE, leftPos + 25, topPos + 21, 0, 299, 0,
                    108, 109, 512, 512);

            if (photoStack.getItem() instanceof PhotographItem photographItem) {
                @Nullable Either<String, ResourceLocation> idOrTexture = photographItem.getIdOrTexture(photoStack);
                if (idOrTexture != null) {
                    MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                    ExposureClient.getExposureRenderer().render(idOrTexture, false, false, guiGraphics.pose(),
                            bufferSource, this.leftPos + 31, this.topPos + 27, this.leftPos + 127, this.topPos + 123,
                            0, 0, 1, 1, LightTexture.FULL_BRIGHT, 255, 255, 255, 255);
                    bufferSource.endBatch();
                }
            }
        }

        if (pages.size() >= 2) {
            AlbumItem.AlbumPage page = pages.get(1);
            ItemStack photoStack = page.photo();

            guiGraphics.blit(TEXTURE, leftPos + 166, topPos + 21, 0, 299, 0,
                    108, 109, 512, 512);

            if (photoStack.getItem() instanceof PhotographItem photographItem) {
                @Nullable Either<String, ResourceLocation> idOrTexture = photographItem.getIdOrTexture(photoStack);
                if (idOrTexture != null) {
                    MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                    ExposureClient.getExposureRenderer().render(idOrTexture, false, false, guiGraphics.pose(),
                            bufferSource, this.leftPos + 172, this.topPos + 27, this.leftPos + 268, this.topPos + 123,
                            0, 0, 1, 1, LightTexture.FULL_BRIGHT, 255, 255, 255, 255);
                    bufferSource.endBatch();
                }
            }
        }
    }
}
