package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.entity.stand.EquipmentStand;
import io.github.mortuusars.mortaar.Platform;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class EquipmentStandRenderer<T extends EquipmentStand> extends EntityRenderer<T> {
    public static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/empty.png");
    public static final float MOUNT_SCALE = 0.9f;

    protected final BlockRenderDispatcher blockRenderer;

    public EquipmentStandRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(T entity) {
        return TEXTURE_LOCATION;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        float hurtTime = (float) entity.getHurtTime() - partialTick;
        float damage = Math.max(0, entity.getDamage() - partialTick);
        if (hurtTime > 0.0F) {
            float rotation = Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float) entity.getHurtDirection();
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        }

        float entityPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        renderStand(entity, entityYaw, entityPitch, partialTick, poseStack, bufferSource, packedLight);
        renderMountWithItem(entity, entityYaw, entityPitch, partialTick, poseStack, bufferSource, packedLight);
    }

    protected void renderStand(T entity, float entityYaw, float entityPitch, float partialTick,
                               PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        if (entity.getVehicle() != null) {
            float vehicleRot = Mth.lerp(partialTick, entity.getVehicle().yRotO, entity.getVehicle().getYRot());
            poseStack.mulPose(Axis.YP.rotationDegrees(-vehicleRot + 45));
        }

        poseStack.translate(-0.5f, 0f, -0.5f);

        ModelResourceLocation modelLocation = ExposureClient.Models.CAMERA_STAND;
        BakedModel model = Platform.Client.getModel(modelLocation);
        blockRenderer.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(RenderType.solid()),
              null, model, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    protected void renderMountWithItem(T entity, float entityYaw, float entityPitch, float partialTick,
                                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 1.125, 0);
        float scale = MOUNT_SCALE;
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw + 180));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entityPitch));

        modifyMountPosition(entity, poseStack);

        poseStack.pushPose();
        poseStack.translate(-0.5f, 0f, -0.5f);
        ModelResourceLocation mountModelLocation = ExposureClient.Models.CAMERA_STAND_MOUNT;
        BakedModel mountModel = Platform.Client.getModel(mountModelLocation);
        blockRenderer.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(RenderType.solid()),
              null, mountModel, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 0.125 * MOUNT_SCALE, 0);
        renderItem(entity, entityYaw, entityPitch, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        poseStack.popPose();
    }

    protected void modifyMountPosition(T entity, PoseStack poseStack) {
    }

    protected void renderItem(T entity, float entityYaw, float entityPitch, float partialTick,
                              PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    }
}
