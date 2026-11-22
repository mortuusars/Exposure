package io.github.mortuusars.exposure.network;

import io.github.mortuusars.exposure.network.neoforge.PacketsImpl;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class Packets {
    public static void sendToServer(Packet packet) {
        PacketsImpl.sendToServer(packet);
    }

    public static void sendToClient(Packet packet, ServerPlayer player) {
        PacketsImpl.sendToClient(packet, player);
    }

    public static void sendToClients(Packet packet, Predicate<ServerPlayer> filter) {
        PacketsImpl.sendToClients(packet, filter);
    }

    public static void sendToAllClients(Packet packet) {
        PacketsImpl.sendToAllClients(packet);
    }

    public static void sendToPlayersNear(Packet packet, ServerLevel level, @Nullable ServerPlayer excluded,
                                         double x, double y, double z, double radius) {
        PacketsImpl.sendToPlayersNear(packet, level, excluded, x, y, z, radius);
    }

    // --

    public static void sendToOtherClients(@NotNull ServerPlayer except, Packet packet) {
        except.level().getServer().getPlayerList().getPlayers().forEach(player -> {
            if (!player.equals(except)) {
                sendToClient(packet, player);
            }
        });
    }

    public static void sendToPlayersNear(Packet packet, ServerLevel level, @Nullable ServerPlayer excluded,
                                         Entity entity, double radius) {
        sendToPlayersNear(packet, level, excluded, entity.getX(), entity.getY(), entity.getZ(), radius);
    }
}
