package io.github.mortuusars.exposure.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CameraItemClientExtensions {
    public static void applyDefaultHoldingPose(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        model.head.xRot += 0.4; // If we turn head down completely - arms will be too low.
        if (arm == HumanoidArm.RIGHT) {
            AnimationUtils.animateCrossbowHold(model.rightArm, model.leftArm, model.head, true);
        } else if (arm == HumanoidArm.LEFT) {
            AnimationUtils.animateCrossbowHold(model.rightArm, model.leftArm, model.head, false);
        }
        model.head.xRot += 0.3;
    }

    public static void applySelfieHoldingPose(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        ModelPart cameraArm = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;

        // Arm follows camera:
        cameraArm.xRot = (-(float)Math.PI / 2F) + model.head.xRot + 0.15F;
        cameraArm.yRot = model.head.yRot + (arm == HumanoidArm.RIGHT ? -0.3f : 0.3f);
        cameraArm.zRot = 0f;

        // Undo arm bobbing:
        AnimationUtils.bobModelPart(cameraArm, entity.tickCount + Minecraft.getInstance().getFrameTime(),
                arm == HumanoidArm.LEFT ? 1.0F : -1.0F);
    }

    public static float itemPropertyFunction(ItemStack stack, ClientLevel clientLevel, LivingEntity livingEntity, int seed) {
        if (stack.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(stack)) {
            if (cameraItem.isInSelfieMode(stack))
                return livingEntity == Minecraft.getInstance().player ? 0.2f : 0.3f;
            else
                return 0.1f;
        }

        return 0f;
    }
}
