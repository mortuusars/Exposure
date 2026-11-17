package io.github.mortuusars.exposure.event;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.ItemFramePhotographRenderer;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.decoration.ItemFrame;

public class ClientEvents {
    public static boolean renderItemFrameItem(ItemFrame itemFrame, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!Config.Client.PHOTOGRAPH_RENDERS_IN_ITEM_FRAME.get()) return false;
        if (!(itemFrame.getItem().getItem() instanceof PhotographItem photographItem)) return false;
        if (photographItem.getFrame(itemFrame.getItem()).identifier().isEmpty()) return false;

        poseStack.pushPose();
        poseStack.scale(2F, 2F, 2F);
        ItemFramePhotographRenderer.render(itemFrame, poseStack, buffer, packedLight, photographItem, itemFrame.getItem());
        poseStack.popPose();

        return true;
    }

    public static void resourcesReloaded() {
        ExposureClient.exposureStore().clear();
        ExposureClient.renderedExposures().clearCache();
        ExposureClient.imageRenderer().resetData();
    }
}
