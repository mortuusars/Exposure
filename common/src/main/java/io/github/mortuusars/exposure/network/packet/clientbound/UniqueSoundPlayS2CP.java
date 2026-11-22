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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record UniqueSoundPlayS2CP(String id, int entityId, SoundEvent sound, SoundSource source,
                                  float volume, float pitch) implements Packet {
    public static final Identifier ID = Exposure.resource("unique_sound_play");
    public static final CustomPacketPayload.Type<UniqueSoundPlayS2CP> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UniqueSoundPlayS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, UniqueSoundPlayS2CP::id,
            ByteBufCodecs.VAR_INT, UniqueSoundPlayS2CP::entityId,
            SoundEvent.DIRECT_STREAM_CODEC, UniqueSoundPlayS2CP::sound,
            ByteBufCodecs.idMapper(i -> SoundSource.values()[i], SoundSource::ordinal), UniqueSoundPlayS2CP::source,
            ByteBufCodecs.FLOAT, UniqueSoundPlayS2CP::volume,
            ByteBufCodecs.FLOAT, UniqueSoundPlayS2CP::pitch,
            UniqueSoundPlayS2CP::new
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
