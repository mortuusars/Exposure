package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.ExposureDataPartPacket;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.network.packet.client.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

public class ClientPackets {
    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ExposureDataPartPacket.ID, new ClientHandler(ExposureDataPartPacket::fromBuffer));

        ClientPlayNetworking.registerGlobalReceiver(ApplyShaderS2CP.ID, new ClientHandler(ApplyShaderS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(StartExposureS2CP.ID, new ClientHandler(StartExposureS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(LoadExposureCommandS2CP.ID, new ClientHandler(LoadExposureCommandS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(ShowExposureS2CP.ID, new ClientHandler(ShowExposureS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(ExposeCommandS2CP.ID, new ClientHandler(ExposeCommandS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(PlayOnePerPlayerSoundS2CP.ID, new ClientHandler(PlayOnePerPlayerSoundS2CP::fromBuffer));
        ClientPlayNetworking.registerGlobalReceiver(StopOnePerPlayerSoundS2CP.ID, new ClientHandler(StopOnePerPlayerSoundS2CP::fromBuffer));
    }

    public static void sendToServer(IPacket<?> packet) {
        ClientPlayNetworking.send(packet.getId(), packet.toBuffer(PacketByteBufs.create()));
    }

    private record ClientHandler(Function<FriendlyByteBuf, IPacket<?>> decodeFunction) implements ClientPlayNetworking.PlayChannelHandler {
        @Override
        public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
            IPacket<?> packet = decodeFunction.apply(buf);
            packet.handle(PacketDirection.TO_CLIENT, null);
        }
    }
}
