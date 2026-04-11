package io.github.mortuusars.exposure.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class Packets {
    @ExpectPlatform
    public static void sendToServer(Packet packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToClient(Packet packet, ServerPlayer player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToClients(Packet packet, Predicate<ServerPlayer> filter) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToAllClients(Packet packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToPlayersNear(Packet packet, ServerLevel level, @Nullable ServerPlayer excluded,
                                         double x, double y, double z, double radius) {
        throw new AssertionError();
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
