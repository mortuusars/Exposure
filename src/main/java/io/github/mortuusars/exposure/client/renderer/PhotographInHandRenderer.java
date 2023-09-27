package io.github.mortuusars.exposure.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.item.PhotographItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PhotographInHandRenderer {
    public static void renderPhotograph(PoseStack poseStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack stack) {
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.38f, 0.38f, 0.38f);
        poseStack.translate(-0.5, -0.5, 0);
        float scale = 1f / PhotographRenderer.SIZE;
        poseStack.scale(scale, scale, -scale);
        Either<String, ResourceLocation> idOrTexture = ((PhotographItem) stack.getItem()).getIdOrTexture(stack);
        PhotographRenderer.renderOnPaper(idOrTexture, poseStack, pBuffer, pCombinedLight, true);
    }
}
