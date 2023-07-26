package io.github.mortuusars.exposure.network;


import io.github.mortuusars.exposure.network.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Predicate;

public class Packets {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("exposure:packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        UpdateActiveCameraPacket.register(CHANNEL, id++);
        ExposureDataPartPacket.register(CHANNEL, id++);

        CameraSetZoomServerboundPacket.register(CHANNEL, id++);
        CameraSetCompositionGuideServerboundPacket.register(CHANNEL, id++);
        CameraSetShutterSpeedServerboundPacket.register(CHANNEL, id++);
        SyncCameraServerboundPacket.register(CHANNEL, id++);
        QueryExposureDataServerboundPacket.register(CHANNEL, id++);

        ApplyShaderClientboundPacket.register(CHANNEL, id++);
        LoadExposureCommandClientboundPacket.register(CHANNEL, id++);
        ExposeCommandClientboundPacket.register(CHANNEL, id++);
        PlayFilmAdvanceSoundClientboundPacket.register(CHANNEL, id++);
        PlayOnePerPlayerSoundClientboundPacket.register(CHANNEL, id++);
        StopOnePerPlayerSoundClientboundPacket.register(CHANNEL, id++);
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToClients(MSG message, Predicate<ServerPlayer> filter) {
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (filter.test(player))
                sendToClient(message, player);
        }
    }

    public static <MSG> void sendToOtherClients(MSG message, ServerPlayer excludedPlayer) {
        sendToClients(message, serverPlayer -> !serverPlayer.equals(excludedPlayer));
    }

    public static <MSG> void sendToOtherClients(MSG message, ServerPlayer excludedPlayer, Predicate<ServerPlayer> filter) {
        sendToClients(message, serverPlayer -> !serverPlayer.equals(excludedPlayer) && filter.test(serverPlayer));
    }
}