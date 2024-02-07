package io.github.mortuusars.exposure.network.forge;


import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.ExposureDataPartPacket;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.network.packet.client.*;
import io.github.mortuusars.exposure.network.packet.server.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class PacketsImpl {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("exposure:packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        // BOTH
        CHANNEL.messageBuilder(ExposureDataPartPacket.class, id++)
                .encoder(ExposureDataPartPacket::toBuffer)
                .decoder(ExposureDataPartPacket::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        // SERVER
        CHANNEL.messageBuilder(DeactivateCamerasInHandC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeactivateCamerasInHandC2SP::toBuffer)
                .decoder(DeactivateCamerasInHandC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(CameraSetZoomC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraSetZoomC2SP::toBuffer)
                .decoder(CameraSetZoomC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(CameraSetCompositionGuideC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraSetCompositionGuideC2SP::toBuffer)
                .decoder(CameraSetCompositionGuideC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(CameraSetFlashModeC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraSetFlashModeC2SP::toBuffer)
                .decoder(CameraSetFlashModeC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(CameraSetShutterSpeedC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraSetShutterSpeedC2SP::toBuffer)
                .decoder(CameraSetShutterSpeedC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(CameraInHandAddFrameC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraInHandAddFrameC2SP::toBuffer)
                .decoder(CameraInHandAddFrameC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(CameraSetSelfieModeC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CameraSetSelfieModeC2SP::toBuffer)
                .decoder(CameraSetSelfieModeC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(QueryExposureDataC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(QueryExposureDataC2SP::toBuffer)
                .decoder(QueryExposureDataC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        CHANNEL.messageBuilder(AlbumSyncNoteC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AlbumSyncNoteC2SP::toBuffer)
                .decoder(AlbumSyncNoteC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        CHANNEL.messageBuilder(AlbumSignC2SP.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AlbumSignC2SP::toBuffer)
                .decoder(AlbumSignC2SP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();

        // CLIENT
        CHANNEL.messageBuilder(ApplyShaderS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ApplyShaderS2CP::toBuffer)
                .decoder(ApplyShaderS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(StartExposureS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StartExposureS2CP::toBuffer)
                .decoder(StartExposureS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(LoadExposureCommandS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LoadExposureCommandS2CP::toBuffer)
                .decoder(LoadExposureCommandS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(ShowExposureS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ShowExposureS2CP::toBuffer)
                .decoder(ShowExposureS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(ExposeCommandS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ExposeCommandS2CP::toBuffer)
                .decoder(ExposeCommandS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(PlayOnePerPlayerSoundS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayOnePerPlayerSoundS2CP::toBuffer)
                .decoder(PlayOnePerPlayerSoundS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
        CHANNEL.messageBuilder(StopOnePerPlayerSoundS2CP.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StopOnePerPlayerSoundS2CP::toBuffer)
                .decoder(StopOnePerPlayerSoundS2CP::fromBuffer)
                .consumerMainThread(PacketsImpl::handlePacket)
                .add();
    }

    public static void sendToServer(IPacket<?> packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToClient(IPacket<?> packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    private static <T extends IPacket<T>> void handlePacket(T packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        packet.handle(direction(context.getDirection()), context.getSender());
    }

    private static PacketDirection direction(NetworkDirection direction) {
        if (direction == NetworkDirection.PLAY_TO_SERVER)
            return PacketDirection.TO_SERVER;
        else if (direction == NetworkDirection.PLAY_TO_CLIENT)
            return PacketDirection.TO_CLIENT;
        else
            throw new IllegalStateException("Can only convert direction for Client/Server, not others.");
    }
}