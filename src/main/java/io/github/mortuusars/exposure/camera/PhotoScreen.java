package io.github.mortuusars.exposure.camera;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhotoScreen extends Screen {

    private String id;
    @Nullable
    private ExposureSavedData exposureData;
//    private MapItemSavedData[][] mps;

    public PhotoScreen(String id) {
        super(Component.empty());

        this.id = id;
        this.exposureData = ExposureStorage.get(id).orElse(null);

//        if (Minecraft.getInstance().level != null) {
//            int rowsAndColumns = (int) Math.sqrt(parts);
//            mps = new MapItemSavedData[rowsAndColumns][rowsAndColumns];
//
//            for (int column = 0; column < rowsAndColumns; column++) {
//                for (int row = 0; row < rowsAndColumns; row++) {
//
//                    MapItemSavedData mapData = Minecraft.getInstance().level.getMapData(id + "_" + column + row);
//
//                    if (mapData != null)
//                        mps[column][row] = mapData;
//                    else
//                        mps[column][row] = MapItemSavedData.createFresh(0d, 0d, (byte) 0, false, false, Minecraft.getInstance().level.dimension());
//                }
//            }
//        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(poseStack);
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);

        fill(poseStack, 0, 0, width, height, 0xAAAAAAAA);

        if (exposureData != null) {

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            float scale = 1f / (exposureData.getHeight() / 256f);

            int centerX = this.width / 2;
            int centerY = this.height / 2;
            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);
            poseStack.translate((width - 256) / 2f / scale, (height - 256) / 2f / scale, 0);
            ExposureRenderer.render(poseStack, bufferSource, id, exposureData, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
    //        float scale = 2f / mps.length;
    //        int startX = (int) (centerX - (mps.length * 128 * scale) / 2);
    //        int startY = (int) (centerY - (mps.length * 128 * scale) / 2);
    //
    //        for (int column = 0; column < mps.length; column++) {
    //            for (int row = 0; row < mps[0].length; row++) {
    //                poseStack.pushPose();
    //                poseStack.translate(startX + (column * 128 * scale), startY + (row * 128 * scale), 0);
    //                poseStack.scale(scale, scale, scale);
    //                Minecraft.getInstance().gameRenderer.getMapRenderer().render(poseStack, bufferSource,
    //                        row + column * 4, mps[column][row], true, LightTexture.FULL_BRIGHT);
    //                poseStack.popPose();
    //            }
    //        }

            bufferSource.endBatch();
        }
        else {
            exposureData = ExposureStorage.get(id).orElse(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
