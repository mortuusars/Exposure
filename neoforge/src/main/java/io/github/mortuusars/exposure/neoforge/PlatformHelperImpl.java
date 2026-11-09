package io.github.mortuusars.exposure.neoforge;

import io.github.mortuusars.exposure.integration.Mods;
import io.github.mortuusars.exposure.neoforge.api.event.ModifyEntityInFrameDataEvent;
import io.github.mortuusars.exposure.neoforge.api.event.FrameAddedEvent;
import io.github.mortuusars.exposure.neoforge.api.event.ModifyFrameExtraDataEvent;
import io.github.mortuusars.exposure.neoforge.integration.CreateIntegration;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public class PlatformHelperImpl {
    public static List<String> getDefaultSpoutDevelopmentColorSequence() {
        return List.of(
                "{FluidName:\"create:potion\",Amount:250,Tag:{Potion:\"minecraft:awkward\"}}",
                "{FluidName:\"create:potion\",Amount:250,Tag:{Potion:\"minecraft:thick\"}}",
                "{FluidName:\"create:potion\",Amount:250,Tag:{Potion:\"minecraft:mundane\"}}");
    }

    public static List<String> getDefaultSpoutDevelopmentBWSequence() {
        return List.of(
                "{FluidName:\"minecraft:water\",Amount:250}");
    }

    public static boolean isCreateDeployer(Player player, InteractionHand hand) {
        if (Mods.CREATE.isLoaded()) {
            return CreateIntegration.isDeployer(player);
        }
        return false;
    }

    // --

    public static void postModifyEntityInFrameExtraDataEvent(CameraHolder cameraHolder, ItemStack camera, LivingEntity entityInFrame, ExtraData data) {
        NeoForge.EVENT_BUS.post(new ModifyEntityInFrameDataEvent(cameraHolder, camera, entityInFrame, data));
    }

    public static void postModifyFrameExtraDataEvent(CameraHolder cameraHolder, ItemStack camera, CaptureParameters captureParameters,
                                                     List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame, ExtraData data) {
        NeoForge.EVENT_BUS.post(new ModifyFrameExtraDataEvent(cameraHolder, camera, captureParameters, positionsInFrame, entitiesInFrame, data));
    }

    public static void postFrameAddedEvent(CameraHolder cameraHolder, ItemStack camera, Frame frame, List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        NeoForge.EVENT_BUS.post(new FrameAddedEvent(cameraHolder, camera, frame, positionsInFrame, entitiesInFrame));
    }
}
