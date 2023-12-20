package io.github.mortuusars.exposure.forge.item;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.CameraItemClientExtensions;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class CameraItemForgeClientExtensions implements IClientItemExtensions {
    public static final CameraItemForgeClientExtensions INSTANCE = new CameraItemForgeClientExtensions();

    private CameraItemForgeClientExtensions() {
    }

    public final HumanoidModel.ArmPose CAMERA_ARM_POSE = HumanoidModel.ArmPose.create("camera", true,
            CameraItemClientExtensions::applyDefaultHoldingPose);
    public final HumanoidModel.ArmPose CAMERA_SELFIE_POSE = HumanoidModel.ArmPose.create("camera_selfie", false,
            CameraItemClientExtensions::applySelfieHoldingPose);

    @Override
    public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
        if (entityLiving instanceof Player
                && itemStack.getItem() instanceof CameraItem cameraItem
                && cameraItem.isActive(itemStack)) {
            if (cameraItem.isInSelfieMode(itemStack))
                return CAMERA_SELFIE_POSE;
            else
                return CAMERA_ARM_POSE;
        }

        return HumanoidModel.ArmPose.ITEM;
    }
}
