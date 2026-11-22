package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.render.state.CameraOperatorRenderState;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    void extractCameraOperatorRenderState(T entity, S reusedState, float partialTick, CallbackInfo ci) {
        if (reusedState instanceof CameraOperatorRenderState reusedOperatorState) {
            if (entity instanceof CameraOperator cameraOperator) {
                reusedOperatorState.setExposureCamera(cameraOperator.getActiveExposureCamera());
                reusedOperatorState.setIsCurrentPlayer(Minecrft.player().equals(entity));
                reusedOperatorState.setIsCameraEntity(Minecrft.get().cameraEntity == entity);
                reusedOperatorState.setExposureCameraActionAnim(cameraOperator.getExposureCameraActionAnim(partialTick));
            }
            if (entity instanceof CameraHolder holder && CameraInHand.find(holder) instanceof CameraInHand camera) {
                reusedOperatorState.setFoundCameraInHand(camera);
            }
        }
    }
}
