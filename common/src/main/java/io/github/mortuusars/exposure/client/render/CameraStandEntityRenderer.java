package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.render.state.CameraStandEntityRenderState;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


public class CameraStandEntityRenderer <T extends CameraStandEntity> extends EntityRenderer<T, CameraStandEntityRenderState> {
    public static final float MOUNT_SCALE = 0.9f;

    protected final ItemModelResolver itemResolver;

    public CameraStandEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemResolver = context.getItemModelResolver();
    }

    @Override
    public @NotNull CameraStandEntityRenderState createRenderState() {
        return new CameraStandEntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, CameraStandEntityRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        reusedState.hurtDir = entity.getHurtDir();
        reusedState.hurtTime = (float)entity.getHurtTime() - partialTick;
        reusedState.damageTime = Math.max(0, entity.getDamage() - partialTick);
        reusedState.entityPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        reusedState.entityYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        reusedState.inVehicle = entity.getVehicle() != null;
        reusedState.isMalfunctioned = entity.isMalfunctioned();
        ItemStack camera = entity.getCamera();
        reusedState.cameraScale = camera.getItem() instanceof CameraItem cameraItem ? cameraItem.getScaleOnStand() : MOUNT_SCALE;
        itemResolver.updateForNonLiving(reusedState.cameraModel, camera, ItemDisplayContext.NONE, entity);
        if (reusedState.inVehicle) {
            reusedState.vehicleRot = Mth.lerp(partialTick, entity.getVehicle().yRotO, entity.getVehicle().getYRot());
        }

    }

    @Override
    public void submit(CameraStandEntityRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {

        float hurtTime = state.hurtTime;
        float damage = state.damageTime;
        if (hurtTime > 0.0F) {
            float rotation = Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float) state.hurtDir;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        }

        renderStand(state, poseStack, nodeCollector, state.lightCoords);
        renderMount(state, poseStack, nodeCollector, state.lightCoords);
        if (!state.cameraModel.isEmpty()) {
            renderCamera(state, poseStack, nodeCollector, state.lightCoords);
        }
    }

    private void renderStand(CameraStandEntityRenderState state, PoseStack poseStack, SubmitNodeCollector bufferSource, int packedLight) {
        poseStack.pushPose();

        if (state.inVehicle) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-state.vehicleRot + 45));
        }

        poseStack.translate(-0.5f, 0f, -0.5f);

        Identifier modelLocation = ExposureClient.Models.CAMERA_STAND;
        BlockStateModel model = PlatformHelperClient.getModel(modelLocation);
        bufferSource.submitBlockModel(poseStack, RenderTypes.solidMovingBlock(), model, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private void renderMount(CameraStandEntityRenderState state, PoseStack poseStack, SubmitNodeCollector bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 1.125, 0);
        float scale = MOUNT_SCALE;
        poseStack.scale(scale, scale, scale);

        float entityYaw = state.entityYaw;
        float entityPitch = state.entityPitch;

        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw + 180));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entityPitch));

        if (state.isMalfunctioned) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-50));
            poseStack.mulPose(Axis.XP.rotationDegrees(-10));
        }

        poseStack.translate(-0.5f, 0f, -0.5f);
        Identifier mountModelLocation = ExposureClient.Models.CAMERA_STAND_MOUNT;
        BlockStateModel mountModel = PlatformHelperClient.getModel(mountModelLocation);
        bufferSource.submitBlockModel(poseStack, RenderTypes.solidMovingBlock(), mountModel, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private void renderCamera(CameraStandEntityRenderState state, PoseStack poseStack, SubmitNodeCollector bufferSource, int packedLight) {
        poseStack.pushPose();

        float entityYaw = state.entityYaw;
        float entityPitch = state.entityPitch;

        poseStack.translate(0, 1.125, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw + 180));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entityPitch));
        poseStack.translate(0, 0.125 * MOUNT_SCALE, 0);

        if (state.isMalfunctioned) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-50));
            poseStack.mulPose(Axis.XP.rotationDegrees(-15));
        }

        float scale = state.cameraScale;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, 0.5, 0);
        state.cameraModel.submit(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
