package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ShaderApplyS2CP(Optional<Identifier> shaderLocation) implements Packet {
    public static final Identifier ID = Exposure.resource("shader_apply");
    public static final CustomPacketPayload.Type<ShaderApplyS2CP> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ShaderApplyS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), ShaderApplyS2CP::shaderLocation,
            ShaderApplyS2CP::new
    );

    public ShaderApplyS2CP(Identifier location) {
        this(Optional.of(location));
    }

    public static final ShaderApplyS2CP REMOVE = new ShaderApplyS2CP(Optional.empty());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ClientPacketsHandler.applyShader(this);
        return true;
    }
}
