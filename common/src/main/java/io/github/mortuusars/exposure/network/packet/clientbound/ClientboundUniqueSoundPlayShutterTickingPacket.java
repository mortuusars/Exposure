package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.sound.UniqueSoundManager;
import io.github.mortuusars.exposure.client.sound.instance.ShutterTickingSoundInstance;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.jetbrains.annotations.NotNull;

public record ClientboundUniqueSoundPlayShutterTickingPacket(int entityId,
                                                CameraId cameraId,
                                                float volume,
                                                float pitch,
                                                int durationTicks) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("unique_sound_play_shutter_ticking");
    public static final Type<ClientboundUniqueSoundPlayShutterTickingPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUniqueSoundPlayShutterTickingPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClientboundUniqueSoundPlayShutterTickingPacket::entityId,
            CameraId.STREAM_CODEC, ClientboundUniqueSoundPlayShutterTickingPacket::cameraId,
            ByteBufCodecs.FLOAT, ClientboundUniqueSoundPlayShutterTickingPacket::volume,
            ByteBufCodecs.FLOAT, ClientboundUniqueSoundPlayShutterTickingPacket::pitch,
            ByteBufCodecs.VAR_INT, ClientboundUniqueSoundPlayShutterTickingPacket::durationTicks,
            ClientboundUniqueSoundPlayShutterTickingPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Client.playShutterTickingSound(entityId, cameraId, volume, pitch, durationTicks);
        return true;
    }

    public static class Client {
        public static void playShutterTickingSound(int entityId, CameraId cameraId, float volume, float pitch, int durationTicks) {
            Entity entity = Minecrft.playerOrThrow().level().getEntity(entityId);
            if (entity != null) {
                SoundInstance instance = new ShutterTickingSoundInstance(entity, cameraId,
                        Exposure.SoundEvents.SHUTTER_TICKING.get(), entity.getSoundSource(),
                        volume, pitch, durationTicks);
                UniqueSoundManager.play(cameraId.toString(), instance);
            }
        }
    }
}
