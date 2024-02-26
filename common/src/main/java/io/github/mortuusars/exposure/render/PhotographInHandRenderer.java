package io.github.mortuusars.exposure.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

public class PhotographInHandRenderer {
    public static void renderPhotograph(PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, ItemStack stack) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.38f, 0.38f, 0.38f);
        poseStack.translate(-0.5, -0.5, 0);
        float scale = 1f / ExposureClient.getExposureRenderer().getSize();
        poseStack.scale(scale, scale, -scale);

        PhotographRenderer.render(stack, true, false, poseStack, bufferSource,
                combinedLight, 255, 255, 255, 255);

//        @Nullable Either<String, ResourceLocation> idOrTexture;

//        if (stack.getItem() instanceof PhotographItem photographItem)
//            idOrTexture = photographItem.getIdOrTexture(stack);
//        else if (stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem)
//            idOrTexture = stackedPhotographsItem.getFirstIdOrTexture(stack);
//        else throw new IllegalArgumentException(stack + " cannot be rendered as a Photograph.");


//        if (idOrTexture != null) {
//            if (stack.is(Exposure.Items.AGED_PHOTOGRAPH.get())) {
//                ExposureClient.getExposureRenderer().renderAgedOnPaper(idOrTexture, ExposurePixelModifiers.AGED,
//                        poseStack, bufferSource,
//                        0, 0, ExposureClient.getExposureRenderer().getSize(), ExposureClient.getExposureRenderer().getSize(), 0, 0, 1, 1,
//                        combinedLight, 255, 255, 255, 255, false);
//            }
//            else {
//                ExposureClient.getExposureRenderer().renderOnPaper(idOrTexture, ExposurePixelModifiers.EMPTY, poseStack, bufferSource,
//                        0, 0, ExposureClient.getExposureRenderer().getSize(), ExposureClient.getExposureRenderer().getSize(), 0, 0, 1, 1,
//                        combinedLight, 255, 255, 255, 255, false);
//            }
//        }
//        else {
//            ExposureClient.getExposureRenderer().renderPaperTexture(poseStack, bufferSource,
//                    0, 0, ExposureClient.getExposureRenderer().getSize(), ExposureClient.getExposureRenderer().getSize(), 0, 0, 1, 1,
//                    combinedLight, 255, 255, 255, 255);
//        }
    }
}
