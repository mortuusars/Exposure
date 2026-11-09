package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.fabric.api.event.ModifyEntityInFrameExtraDataCallback;
import io.github.mortuusars.exposure.fabric.api.event.FrameAddedCallback;
import io.github.mortuusars.exposure.fabric.api.event.ModifyFrameExtraDataCallback;
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

public class PlatformHelperImpl {
    public static List<String> getDefaultSpoutDevelopmentColorSequence() {
        return List.of(
                "{FluidName:\"create:potion\",Amount:27000,Tag:{Potion:\"minecraft:awkward\"}}",
                "{FluidName:\"create:potion\",Amount:27000,Tag:{Potion:\"minecraft:thick\"}}",
                "{FluidName:\"create:potion\",Amount:27000,Tag:{Potion:\"minecraft:mundane\"}}");
    }

    public static List<String> getDefaultSpoutDevelopmentBWSequence() {
        return List.of(
                "{FluidName:\"minecraft:water\",Amount:27000}");
    }

    public static boolean isCreateDeployer(Player player, InteractionHand hand) {
        return false;
    }

    // --

    public static void postModifyEntityInFrameExtraDataEvent(CameraHolder cameraHolder, ItemStack camera, LivingEntity entityInFrame, ExtraData data) {
        ModifyEntityInFrameExtraDataCallback.EVENT.invoker().modifyEntityInFrameData(cameraHolder, camera, entityInFrame, data);
    }

    public static void postModifyFrameExtraDataEvent(CameraHolder cameraHolder, ItemStack camera, CaptureParameters captureParameters,
                                                List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame, ExtraData data) {
        ModifyFrameExtraDataCallback.EVENT.invoker().modifyFrameExtraData(cameraHolder, camera, captureParameters, positionsInFrame, entitiesInFrame, data);
    }

    public static void postFrameAddedEvent(CameraHolder cameraHolder, ItemStack camera, Frame frame,
                                           List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        FrameAddedCallback.EVENT.invoker().frameAdded(cameraHolder, camera, frame, positionsInFrame, entitiesInFrame);
    }
}
