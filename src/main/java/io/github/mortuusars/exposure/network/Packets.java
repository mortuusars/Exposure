package io.github.mortuusars.exposure.network;


import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderNew;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderServer;
import io.github.mortuusars.exposure.network.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Packets {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("exposure:packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        CHANNEL.messageBuilder(ViewfinderNew.UpdateViewfinderPacket.class, id++)
                .encoder(ViewfinderNew.UpdateViewfinderPacket::toBuffer)
                .decoder(ViewfinderNew.UpdateViewfinderPacket::fromBuffer)
                .consumerMainThread(ViewfinderNew.UpdateViewfinderPacket::handle)
                .add();


        CHANNEL.messageBuilder(ViewfinderServer.ClientboundUpdateViewfinderPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ViewfinderServer.ClientboundUpdateViewfinderPacket::toBuffer)
                .decoder(ViewfinderServer.ClientboundUpdateViewfinderPacket::fromBuffer)
                .consumerMainThread(ViewfinderServer.ClientboundUpdateViewfinderPacket::handleClientside)
                .add();

        CHANNEL.messageBuilder(ClientboundApplyShaderPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundApplyShaderPacket::toBuffer)
                .decoder(ClientboundApplyShaderPacket::fromBuffer)
                .consumerMainThread(ClientboundApplyShaderPacket::handle)
                .add();

        CHANNEL.messageBuilder(ServerboundQueryExposureDataPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundQueryExposureDataPacket::toBuffer)
                .decoder(ServerboundQueryExposureDataPacket::fromBuffer)
                .consumerMainThread(ServerboundQueryExposureDataPacket::handle)
                .add();

        CHANNEL.messageBuilder(ServerboundUpdateCameraPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundUpdateCameraPacket::toBuffer)
                .decoder(ServerboundUpdateCameraPacket::fromBuffer)
                .consumerMainThread(ServerboundUpdateCameraPacket::handle)
                .add();

        CHANNEL.messageBuilder(ServerboundSyncCameraPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundSyncCameraPacket::toBuffer)
                .decoder(ServerboundSyncCameraPacket::fromBuffer)
                .consumerMainThread(ServerboundSyncCameraPacket::handle)
                .add();

        CHANNEL.messageBuilder(ExposureDataPartPacket.class, id++)
                .encoder(ExposureDataPartPacket::toBuffer)
                .decoder(ExposureDataPartPacket::fromBuffer)
                .consumerMainThread(ExposureDataPartPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}