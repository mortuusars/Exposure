package io.github.mortuusars.exposure.network;


import com.google.common.base.Preconditions;
import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class Packets {
    @ExpectPlatform
    public static void sendToServer(IPacket<?> packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToClient(IPacket<?> packet, ServerPlayer player) {
        throw new AssertionError();
    }

    public static void sendToClients(IPacket<?> packet, ServerPlayer origin, Predicate<ServerPlayer> filter) {
        Preconditions.checkState(origin.getServer() != null, "Server cannot be null");
        for (ServerPlayer player : origin.getServer().getPlayerList().getPlayers()) {
            if (filter.test(player))
                sendToClient(packet, player);
        }
    }

    public static void sendToOtherClients(IPacket<?> packet, ServerPlayer excludedPlayer) {
        sendToClients(packet, excludedPlayer, serverPlayer -> !serverPlayer.equals(excludedPlayer));
    }

    public static void sendToOtherClients(IPacket<?> packet, ServerPlayer excludedPlayer, Predicate<ServerPlayer> filter) {
        sendToClients(packet, excludedPlayer, serverPlayer -> !serverPlayer.equals(excludedPlayer) && filter.test(serverPlayer));
    }
}