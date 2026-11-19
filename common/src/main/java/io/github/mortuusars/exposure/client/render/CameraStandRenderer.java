package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.world.entity.stand.CameraStand;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CameraStandRenderer<T extends CameraStand> extends EquipmentStandRenderer<T> {
    public CameraStandRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void modifyMountPosition(T entity, PoseStack poseStack) {
        if (entity.isMalfunctioned()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-50));
            poseStack.mulPose(Axis.XP.rotationDegrees(-10));
        }
    }

    protected void renderItem(T entity, float entityYaw, float entityPitch, float partialTick,
                              PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        ItemStack item = entity.getCamera();
        float scale = item.getItem() instanceof CameraItem cameraItem ? cameraItem.getScaleOnStand() : 0.4f;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, 0.5, 0);
        Minecrft.get().getItemRenderer().renderStatic(item, ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), 0);
        poseStack.popPose();
    }
}
