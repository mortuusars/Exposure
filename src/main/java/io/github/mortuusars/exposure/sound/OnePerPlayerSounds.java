package io.github.mortuusars.exposure.sound;

import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.PlayOnePerPlayerSoundClientboundPacket;
import io.github.mortuusars.exposure.network.packet.client.StopOnePerPlayerSoundClientboundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class OnePerPlayerSounds {
    public static void play(Player sourcePlayer, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        if (sourcePlayer.getLevel().isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->
                    () -> OnePerPlayerSoundsClient.play(sourcePlayer, soundEvent, source, volume, pitch));
        }
        else if (sourcePlayer instanceof ServerPlayer serverSourcePlayer) {
            Packets.sendToOtherClients(new PlayOnePerPlayerSoundClientboundPacket(serverSourcePlayer.getUUID(),
                            soundEvent,source, volume, pitch),
                    serverSourcePlayer,
                    serverPlayer -> serverSourcePlayer.distanceTo(serverPlayer) < soundEvent.getRange(1f));
        }
    }

    public static void stop(Player sourcePlayer, SoundEvent soundEvent) {
        if (sourcePlayer.getLevel().isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->
                    () -> OnePerPlayerSoundsClient.stop(sourcePlayer, soundEvent));
        }
        else if (sourcePlayer instanceof ServerPlayer serverSourcePlayer) {
            Packets.sendToOtherClients(new StopOnePerPlayerSoundClientboundPacket(serverSourcePlayer.getUUID(), soundEvent),
                    serverSourcePlayer, serverPlayer -> serverSourcePlayer.distanceTo(serverPlayer) < soundEvent.getRange(1f));
        }
    }
}
