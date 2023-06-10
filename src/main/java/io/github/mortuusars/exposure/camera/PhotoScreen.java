package io.github.mortuusars.exposure.camera;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.ArrayList;
import java.util.List;

public class PhotoScreen extends Screen {

    private List<MapItemSavedData> maps;

    public PhotoScreen(String id, int parts) {
        super(Component.empty());

        if (Minecraft.getInstance().level != null) {
            maps = new ArrayList<>();

            if (parts == 1) {
                MapItemSavedData mapData = Minecraft.getInstance().level.getMapData(id);
                Preconditions.checkArgument(mapData != null);
                maps.add(mapData);
            }
            else {
                for (int i = 1; i <= parts; i++) {
                    MapItemSavedData mapData = Minecraft.getInstance().level.getMapData(id + "_" + i);

                    if (mapData == null)
                        break;

                    maps.add(mapData);
                }
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(poseStack);
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);

        MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        float scale = 1f;

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        for (int i = 0; i < maps.size(); i++) {

            int ind = i + 1;
            double x = centerX - (ind == 1 || ind == 3 ? 128 * scale : 0);
            double y = centerY - (ind == 1 || ind == 2 ? 128 * scale : 0);

            poseStack.pushPose();
//            poseStack.translate(centerX, centerY, 0);
//            poseStack.scale(scale, scale, scale);
//            poseStack.translate(-64, -64, 0);

            poseStack.translate(x, y, 0);
            minecraft.gameRenderer.getMapRenderer().render(poseStack, multibuffersource$buffersource,
                    i, maps.get(i), true, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }

//        for (int i = 1; i <= 4; i++) {
//            MapItemSavedData mapData = minecraft.level.getMapData("photo_" + i);
//
//            double x = centerX - (i == 1 || i == 3 ? 128 * scale : 0);
//            double y = centerY - (i == 1 || i == 2 ? 128 * scale : 0);
//
//            poseStack.pushPose();
//            poseStack.translate(x, y, 0);
//            minecraft.gameRenderer.getMapRenderer().render(poseStack, multibuffersource$buffersource,
//                    i, mapData, true, LightTexture.FULL_BRIGHT);
//            poseStack.popPose();
//        }

        multibuffersource$buffersource.endBatch();
    }

    public void renderPhoto(PoseStack poseStack) {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
