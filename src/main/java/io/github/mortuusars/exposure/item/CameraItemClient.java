package io.github.mortuusars.exposure.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class CameraItemClient implements IClientItemExtensions {
    public static final CameraItemClient INSTANCE = new CameraItemClient();
    private CameraItemClient() {}

    public final HumanoidModel.ArmPose CAMERA_ARM_POSE = HumanoidModel.ArmPose.create("camera", true,
            CameraItemClient::apply);

    @Override
    public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
        if (entityLiving instanceof Player player && CameraInHand.isActive(player))
            return CAMERA_ARM_POSE;
        else
            return HumanoidModel.ArmPose.ITEM;
    }

    public static void apply(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        model.head.xRot += 0.4; // If we turn head down completely - arms will be too low.
        if (arm == HumanoidArm.RIGHT) {
            AnimationUtils.animateCrossbowHold(model.rightArm, model.leftArm, model.head, true);
        }
        else if (arm == HumanoidArm.LEFT) {
            AnimationUtils.animateCrossbowHold(model.rightArm, model.leftArm, model.head, false);
        }
        model.head.xRot += 0.3;
    }

    public static float itemPropertyFunction(ItemStack stack, ClientLevel clientLevel, LivingEntity livingEntity, int seed) {
        if (stack.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(stack))
            return 0.1f;

        return 0f;
    }
}
