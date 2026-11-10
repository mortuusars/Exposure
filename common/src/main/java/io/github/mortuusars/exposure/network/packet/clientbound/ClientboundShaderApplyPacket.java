package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ClientboundShaderApplyPacket(Optional<ResourceLocation> shaderLocation) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("shader_apply");
    public static final CustomPacketPayload.Type<ClientboundShaderApplyPacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ClientboundShaderApplyPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ClientboundShaderApplyPacket::shaderLocation,
            ClientboundShaderApplyPacket::new
    );

    public ClientboundShaderApplyPacket(ResourceLocation location) {
        this(Optional.of(location));
    }

    public static final ClientboundShaderApplyPacket REMOVE = new ClientboundShaderApplyPacket(Optional.empty());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        Client.applyShader(shaderLocation);
        return true;
    }

    public static class Client {
        public static void applyShader(Optional<ResourceLocation> shaderLocation) {
            shaderLocation.ifPresentOrElse(
                  shader -> Minecrft.get().gameRenderer.loadEffect(shader),
                  () -> Minecrft.get().gameRenderer.shutdownEffect());
        }
    }
}
