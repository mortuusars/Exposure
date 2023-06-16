package io.github.mortuusars.exposure.camera;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.mortuusars.exposure.client.render.ExposureRenderer;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExposureScreen extends Screen {

    private String id;
    @Nullable
    private ExposureSavedData exposureData;
//    private MapItemSavedData[][] mps;

    public ExposureScreen(String id) {
        super(Component.empty());

        this.id = id;
        this.exposureData = ExposureStorage.CLIENT.getOrQuery(id).orElse(null);

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

//        fill(poseStack, 0, 0, width, height, 0x90000000);

        if (exposureData != null) {

            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            float scale = (height - (height / 6f)) / exposureData.getHeight();

//            float scale = 1f / (exposureData.getHeight() / ((float)height));
            float x = (width - exposureData.getWidth()) / 2f / scale;
            float y = (height - exposureData.getHeight()) / 2f / scale;

            poseStack.pushPose();

            // Move to center
            poseStack.translate(width / 2f, height / 2f, 0);
            // Scale
            poseStack.scale(scale, scale, scale);
            // Set origin point to center (for scale)
            poseStack.translate(exposureData.getWidth() / -2d, exposureData.getHeight() / -2d, 0);
//            poseStack.translate(x, y, 0);

            fill(poseStack, -8, -8, exposureData.getWidth() + 8, exposureData.getHeight() + 8, 0xFFDDDDDD);
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
            exposureData = ExposureStorage.CLIENT.getOrQuery(id).orElse(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
