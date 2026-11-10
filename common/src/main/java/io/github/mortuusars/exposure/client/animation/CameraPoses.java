package io.github.mortuusars.exposure.client.animation;

import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.mortaar.util.Easing;
import net.minecraft.client.CameraType;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class CameraPoses {
    public void applyHolding(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {

        boolean rightHanded = arm == HumanoidArm.RIGHT;

        ModelPart mainHand = rightHanded ? model.rightArm : model.leftArm;
        ModelPart offHand = rightHanded ? model.leftArm : model.rightArm;

        model.head.xRot += 0.4f; // Applying part of head rotation. If we turn head down completely - arms will be too low.
        model.head.xRot = Math.clamp(model.head.xRot, -1F, 1.15F); // Look up/down limit

        mainHand.yRot = (rightHanded ? -0.3F : 0.3F) + model.head.yRot;
        offHand.yRot = (rightHanded ? 0.5F : -0.5F) + model.head.yRot;
        mainHand.xRot = model.head.xRot - 1.5F;
        offHand.xRot = model.head.xRot - 1.5F;
        float actionAnim = getCameraActionAnim(entity);
        offHand.xRot += (actionAnim * 0.1F) * (rightHanded ? 1 : -1);
        offHand.yRot += (actionAnim * 0.1F) * (rightHanded ? 1 : -1);
        model.head.xRot += 0.3f; // Applying rest of head rotation after arms

        model.hat.copyFrom(model.head);
    }

    public void applySelfie(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm, boolean undoArmBobbing) {
        ModelPart cameraArm = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;

        // Arm follows camera:
        cameraArm.xRot = (model.head.xRot + Math.abs(model.head.xRot * 0.13f)) + (-(float) Math.PI / 2F);
        cameraArm.yRot = model.head.yRot;
        if (Minecrft.get().cameraEntity == entity) {
            cameraArm.yRot += (arm == HumanoidArm.RIGHT ? -0.25f : 0.25f);
        }

        if (model.head.xRot <= 0) {
            cameraArm.zRot = (model.head.xRot * 0.15f) * (arm == HumanoidArm.RIGHT ? -1 : 1);
        } else {
            cameraArm.zRot = (model.head.xRot * 0.22f) * (arm == HumanoidArm.RIGHT ? -1 : 1);
        }
    }

    public void applyDisassembled(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        if (Minecrft.player().equals(entity) && Minecrft.options().getCameraType() == CameraType.FIRST_PERSON) {
            return;
        }

        model.head.xRot += 0.4f; // Applying part of head rotation. If we turn head down completely - arms will be too low.
        model.head.xRot = Math.clamp(model.head.xRot, -0.75F, 0.75F); // Look up/down limit

        boolean rightHanded = arm == HumanoidArm.RIGHT;

        ModelPart mainHand = rightHanded ? model.rightArm : model.leftArm;
        ModelPart offHand = rightHanded ? model.leftArm : model.rightArm;
        mainHand.yRot = (rightHanded ? -0.6F : 0.6F) + model.head.yRot;
        offHand.yRot = (rightHanded ? 0.6F : -0.6F) + model.head.yRot;
        mainHand.xRot = model.head.xRot - 1.5F;
        offHand.xRot = model.head.xRot - 1.5F;
        float actionAnim = getCameraActionAnim(entity);
        offHand.xRot += (actionAnim * 0.1F) * (rightHanded ? 1 : -1);
        offHand.yRot += (actionAnim * 0.1F) * (rightHanded ? 1 : -1);
        model.head.xRot += 0.3f; // Applying rest of head rotation after arms

        model.hat.copyFrom(model.head);
    }

    public void applyStand(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm, CameraStandEntity stand) {
        boolean rightHanded = arm == HumanoidArm.RIGHT;

        ModelPart mainHand = rightHanded ? model.rightArm : model.leftArm;
        ModelPart offHand = rightHanded ? model.leftArm : model.rightArm;

        // Loot at stand:
        Vec3 direction = entity.getEyePosition().subtract(stand.getEyePosition());
        float yawToStandDegrees = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(direction.x, direction.z)) + 180);
        float bodyRotDegrees = entity.yBodyRot;
        float yawDegrees = Mth.wrapDegrees(bodyRotDegrees + yawToStandDegrees);
        yawDegrees = Mth.clamp(yawDegrees, -60, 60); // Limit head turning. Player is not owl.
        float yaw = (float) Math.toRadians(yawDegrees);
        model.head.yRot = -yaw;

        double distanceXZ = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float pitch = (float)Math.atan2(-direction.y, distanceXZ);
        model.head.xRot = -pitch;

        model.hat.copyFrom(model.head);

        // Arms to stand:
        mainHand.yRot = (rightHanded ? -0.2F : 0.2F) + model.head.yRot;
        offHand.yRot = (rightHanded ? 0.2F : -0.2F) + model.head.yRot;
        mainHand.xRot = -1.2f;
        offHand.xRot = -1.2f;
        float actionAnim = getCameraActionAnim(entity);
        offHand.xRot += (actionAnim * 0.1F) * (rightHanded ? 1 : -1);
        offHand.yRot += (actionAnim * 0.1F) * (rightHanded ? 1 : -1);
    }

    public float getCameraActionProgress(LivingEntity entity) {
        if (entity instanceof CameraOperator operator) {
            float partialTick = Minecrft.get().getTimer().getGameTimeDeltaPartialTick(true);
            return operator.getExposureCameraActionAnim(partialTick);
        }
        return 0F;
    }

    public float getCameraActionAnim(LivingEntity entity) {
        float actionProgress = getCameraActionProgress(entity);
        actionProgress = (float) Easing.OUT_CUBIC.ease(actionProgress);
        return actionProgress > 0.5F ? (1F - actionProgress) : actionProgress;
    }
}