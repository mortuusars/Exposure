package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.client.sound.UniqueSoundManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record UniqueSoundStopS2CP(String id, SoundEvent sound) implements Packet {
    public static final Identifier ID = Exposure.resource("unique_sound_stop");
    public static final CustomPacketPayload.Type<UniqueSoundStopS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UniqueSoundStopS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, UniqueSoundStopS2CP::id,
            SoundEvent.DIRECT_STREAM_CODEC, UniqueSoundStopS2CP::sound,
            UniqueSoundStopS2CP::new
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
