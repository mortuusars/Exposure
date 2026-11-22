package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.neoforge.PlatformHelperImpl;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class PlatformHelper {
    public static @Nullable MinecraftServer getServer() {
        return PlatformHelperImpl.getServer();
    }

    public static boolean canShear(ItemStack stack) {
        return PlatformHelperImpl.canShear(stack);
    }

    public static boolean canStrip(ItemStack stack) {
        return PlatformHelperImpl.canStrip(stack);
    }

    public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        PlatformHelperImpl.openMenu(serverPlayer, menuProvider, extraDataWriter);
    }

    public static List<String> getDefaultSpoutDevelopmentColorSequence() {
        return PlatformHelperImpl.getDefaultSpoutDevelopmentColorSequence();
    }

    public static List<String> getDefaultSpoutDevelopmentBWSequence() {
        return PlatformHelperImpl.getDefaultSpoutDevelopmentBWSequence();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isModLoaded(String modId) {
        return PlatformHelperImpl.isModLoaded(modId);
    }

    public static boolean isInDevEnv() {
        return PlatformHelperImpl.isInDevEnv();
    }

    // --

    public static void postModifyEntityInFrameExtraDataEvent(CameraHolder cameraHolder, ItemStack camera, LivingEntity entityInFrame, ExtraData data) {
        PlatformHelperImpl.postModifyEntityInFrameExtraDataEvent(cameraHolder, camera, entityInFrame, data);
    }

    public static void postModifyFrameExtraDataEvent(CameraHolder cameraHolder, ItemStack camera, CaptureParameters captureParameters,
                                                     List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame, ExtraData data) {
        PlatformHelperImpl.postModifyFrameExtraDataEvent(cameraHolder, camera, captureParameters, positionsInFrame, entitiesInFrame, data);
    }

    public static void postFrameAddedEvent(CameraHolder holder, ItemStack camera, Frame frame,
                                           List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        PlatformHelperImpl.postFrameAddedEvent(holder, camera, frame, positionsInFrame, entitiesInFrame);
    }
}
