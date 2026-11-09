package io.github.mortuusars.exposure;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PlatformHelper {
    @ExpectPlatform
    public static List<String> getDefaultSpoutDevelopmentColorSequence() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<String> getDefaultSpoutDevelopmentBWSequence() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isCreateDeployer(Player player, InteractionHand hand) {
        throw new AssertionError();
    }

    // --

    @ExpectPlatform
    public static void postModifyEntityInFrameExtraDataEvent(CameraHolder cameraHolder, ItemStack camera, LivingEntity entityInFrame, ExtraData data) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void postModifyFrameExtraDataEvent(CameraHolder cameraHolder, ItemStack camera, CaptureParameters captureParameters,
                                                     List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame, ExtraData data) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void postFrameAddedEvent(CameraHolder holder, ItemStack camera, Frame frame,
                                           List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        throw new AssertionError();
    }
}
