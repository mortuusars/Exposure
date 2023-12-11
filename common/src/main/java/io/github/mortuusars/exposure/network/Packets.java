package io.github.mortuusars.exposure.network;


import com.google.common.base.Preconditions;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class Packets {
    @ExpectPlatform
    public static <MSG> void sendToServer(MSG message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        throw new AssertionError();
    }

    public static <MSG> void sendToClients(MSG message, ServerPlayer origin, Predicate<ServerPlayer> filter) {
        Preconditions.checkState(origin.getServer() != null, "Server cannot be null");
        for (ServerPlayer player : origin.getServer().getPlayerList().getPlayers()) {
            if (filter.test(player))
                sendToClient(message, player);
        }
    }

    public static <MSG> void sendToOtherClients(MSG message, ServerPlayer excludedPlayer) {
        sendToClients(message, excludedPlayer, serverPlayer -> !serverPlayer.equals(excludedPlayer));
    }

    public static <MSG> void sendToOtherClients(MSG message, ServerPlayer excludedPlayer, Predicate<ServerPlayer> filter) {
        sendToClients(message, excludedPlayer, serverPlayer -> !serverPlayer.equals(excludedPlayer) && filter.test(serverPlayer));
    }
}