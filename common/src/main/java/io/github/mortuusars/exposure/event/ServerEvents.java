package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public class ServerEvents {
    public static void onServerSave() {
        if (Config.Server.CLEANUP_TIMED_OUT_EXPECTED_EXPOSURES.get()) {
            ExposureServer.exposureRepository().clearExpectedExposuresTimedOutLongAgo();
        }
    }

    public static void serverStarted(MinecraftServer server) {
        Exposure.initServer(server);
    }

    public static void serverStopped(MinecraftServer server) {

    }

    public static void syncDatapack(Stream<ServerPlayer> relevantPlayers) {

    }

    public static void playerTick(ServerPlayer player) {

    }

    public static void itemDrop(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        ItemStack droppedItem = inventory.getSelectedItem();

        if (droppedItem.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(droppedItem)) {
            player.getActiveExposureCameraOptional().ifPresentOrElse(
                    camera -> {
                        player.removeActiveExposureCamera();
                        cameraItem.deactivate(player, droppedItem);
                    },
                    () -> cameraItem.setActive(droppedItem, false));
        }
    }
}
