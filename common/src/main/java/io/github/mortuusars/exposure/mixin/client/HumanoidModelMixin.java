package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.animation.CameraModelPoses;
import io.github.mortuusars.exposure.client.animation.CameraPoses;
import io.github.mortuusars.exposure.client.render.state.CameraOperatorRenderState;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> {
    @Shadow
    @Final
    public ModelPart leftArm;
    @Shadow
    @Final
    public ModelPart rightArm;

    // Allows reducing/removing arm bobbing. Doing the bobbing in reverse does not work correctly - arms shiver for some reason.
    @Unique
    private float exposure$LeftArmBobbingMultiplier = 1F;
    @Unique
    private float exposure$RightArmBobbingMultiplier = 1F;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V", ordinal = 0))
    void onSetupAnim(T renderState, CallbackInfo ci) {
        if (!(renderState instanceof CameraOperatorRenderState operatorState)) {
            exposure$LeftArmBobbingMultiplier = 1F;
            exposure$RightArmBobbingMultiplier = 1F;
            return;
        }

        if (operatorState.getExposureCamera() instanceof Camera camera && camera.getItemStack().getItem() instanceof CameraItem item) {
            @NotNull CameraPoses<T> poses = (CameraPoses<T>) CameraModelPoses.get(item);
            HumanoidArm arm = camera instanceof CameraInHand cameraInHand && cameraInHand.getHand() == InteractionHand.OFF_HAND
                    ? Minecraft.getInstance().options.mainHand().get().getOpposite()
                    : Minecraft.getInstance().options.mainHand().get();

            if (camera instanceof CameraOnStand cameraOnStand) {
                poses.applyStand((HumanoidModel<?>) (Object) this, renderState, arm, cameraOnStand.getStand());
                exposure$LeftArmBobbingMultiplier = 1F;
                exposure$RightArmBobbingMultiplier = 1F;
                return;
            }

            if (item.isInSelfieMode(camera.getItemStack())) {
                poses.applySelfie((HumanoidModel<?>) (Object) this, renderState, arm, false);

                if (arm == HumanoidArm.LEFT) {
                    exposure$LeftArmBobbingMultiplier = 0F;
                    exposure$RightArmBobbingMultiplier = 1F;
                } else {
                    exposure$LeftArmBobbingMultiplier = 1F;
                    exposure$RightArmBobbingMultiplier = 0F;
                }

                return;
            }

            //noinspection ConstantValue
            if (operatorState.isCurrentPlayer() && PlatformHelper.isModLoaded("realcamera")) {
                return;
            }

            poses.applyHolding(((HumanoidModel<?>) (Object) this), renderState, arm);
            exposure$LeftArmBobbingMultiplier = arm == HumanoidArm.LEFT ? 0.1F : 0.5F;
            exposure$RightArmBobbingMultiplier = arm == HumanoidArm.RIGHT ? 0.1F : 0.5F;
            return;
        }

        if (operatorState.getFoundCameraInHand() != null) {
            CameraInHand camera = operatorState.getFoundCameraInHand();

            camera.ifPresent((item, stack) -> {
                @NotNull CameraPoses<T> poses = (CameraPoses<T>) CameraModelPoses.get(item);
                if (item.isDisassembled(stack)) {
                    HumanoidArm arm = camera.getHand() == InteractionHand.OFF_HAND
                            ? Minecraft.getInstance().options.mainHand().get().getOpposite()
                            : Minecraft.getInstance().options.mainHand().get();
                    poses.applyDisassembled((HumanoidModel<?>) (Object) this, renderState, arm);
                }
            });
        }
        exposure$LeftArmBobbingMultiplier = 1F;
        exposure$RightArmBobbingMultiplier = 1F;
    }

    @Redirect(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V"))
    private void removeBobbing(ModelPart modelPart, float ageInTicks, float multiplier) {
        if (exposure$LeftArmBobbingMultiplier != 1F && modelPart == leftArm) {
            multiplier = exposure$LeftArmBobbingMultiplier * -1;
        }
        if (exposure$RightArmBobbingMultiplier != 1F && modelPart == rightArm) {
            multiplier = exposure$RightArmBobbingMultiplier;
        }
        AnimationUtils.bobModelPart(modelPart, ageInTicks, multiplier);
    }
}