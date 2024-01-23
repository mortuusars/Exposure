package io.github.mortuusars.exposure.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.StackedPhotographsItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PhotographInHandRenderer {
    public static void renderPhotograph(PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, ItemStack stack) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.38f, 0.38f, 0.38f);
        poseStack.translate(-0.5, -0.5, 0);
        float scale = 1f / ExposureRenderer.SIZE;
        poseStack.scale(scale, scale, -scale);

        @Nullable Either<String, ResourceLocation> idOrTexture;

        if (stack.getItem() instanceof PhotographItem photographItem)
            idOrTexture = photographItem.getIdOrTexture(stack);
        else if (stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem)
            idOrTexture = stackedPhotographsItem.getFirstIdOrTexture(stack);
        else throw new IllegalArgumentException(stack + " cannot be rendered as a Photograph.");

        if (idOrTexture != null) {
            ExposureClient.getExposureRenderer().renderOnPaper(idOrTexture, poseStack, bufferSource,
                    0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                    combinedLight, 255, 255, 255, 255, false);
        }
        else {
            ExposureClient.getExposureRenderer().renderPaperTexture(poseStack, bufferSource,
                    0, 0, ExposureRenderer.SIZE, ExposureRenderer.SIZE, 0, 0, 1, 1,
                    combinedLight, 255, 255, 255, 255);
        }
    }
}
