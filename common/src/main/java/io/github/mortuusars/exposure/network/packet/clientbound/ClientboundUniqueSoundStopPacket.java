package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.mortaar.network.packet.Packet;
import io.github.mortuusars.exposure.client.sound.UniqueSoundManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ClientboundUniqueSoundStopPacket(String id, SoundEvent sound) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("unique_sound_stop");
    public static final CustomPacketPayload.Type<ClientboundUniqueSoundStopPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUniqueSoundStopPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundUniqueSoundStopPacket::id,
            SoundEvent.DIRECT_STREAM_CODEC, ClientboundUniqueSoundStopPacket::sound,
            ClientboundUniqueSoundStopPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        UniqueSoundManager.stop(id, sound);
        return true;
    }
}
