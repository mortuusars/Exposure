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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ClientboundUniqueSoundPlayPacket(String id, int entityId, SoundEvent sound, SoundSource source,
                                  float volume, float pitch) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("unique_sound_play");
    public static final CustomPacketPayload.Type<ClientboundUniqueSoundPlayPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUniqueSoundPlayPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundUniqueSoundPlayPacket::id,
            ByteBufCodecs.VAR_INT, ClientboundUniqueSoundPlayPacket::entityId,
            SoundEvent.DIRECT_STREAM_CODEC, ClientboundUniqueSoundPlayPacket::sound,
            ByteBufCodecs.idMapper(i -> SoundSource.values()[i], SoundSource::ordinal), ClientboundUniqueSoundPlayPacket::source,
            ByteBufCodecs.FLOAT, ClientboundUniqueSoundPlayPacket::volume,
            ByteBufCodecs.FLOAT, ClientboundUniqueSoundPlayPacket::pitch,
            ClientboundUniqueSoundPlayPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        @Nullable Entity entity = player.level().getEntity(entityId);
        if (entity != null) {
            UniqueSoundManager.play(id, entity, sound, source, volume, pitch);
        }

        return true;
    }
}
