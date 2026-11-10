package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ClientboundShutterOpenedPacket implements Packet {
    public static final ClientboundShutterOpenedPacket INSTANCE = new ClientboundShutterOpenedPacket();

    public static final ResourceLocation ID = Exposure.resource("shutter_opened");
    public static final Type<ClientboundShutterOpenedPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ClientboundShutterOpenedPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ClientboundShutterOpenedPacket() { }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Client.shutterOpened();
        return true;
    }

    public static class Client {
        public static void shutterOpened() {
            if (CameraClient.viewfinder() != null) {
                CameraClient.viewfinder().overlay().startDrawingShutter();
            }
        }
    }
}
