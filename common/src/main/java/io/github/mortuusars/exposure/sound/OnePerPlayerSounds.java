package io.github.mortuusars.exposure.sound;

import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.PlayOnePerPlayerSoundS2CP;
import io.github.mortuusars.exposure.network.packet.client.StopOnePerPlayerSoundS2CP;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class OnePerPlayerSounds {
    public static void play(Player sourcePlayer, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        if (sourcePlayer.getLevel().isClientSide) {
            OnePerPlayerSoundsClient.play(sourcePlayer, soundEvent, source, volume, pitch);
        }
        else if (sourcePlayer instanceof ServerPlayer serverSourcePlayer) {
            Packets.sendToOtherClients(new PlayOnePerPlayerSoundS2CP(serverSourcePlayer.getUUID(),
                            soundEvent,source, volume, pitch),
                    serverSourcePlayer,
                    serverPlayer -> serverSourcePlayer.distanceTo(serverPlayer) < soundEvent.getRange(1f));
        }
    }

    public static void stop(Player sourcePlayer, SoundEvent soundEvent) {
        if (sourcePlayer.getLevel().isClientSide) {
            OnePerPlayerSoundsClient.stop(sourcePlayer, soundEvent);
        }
        else if (sourcePlayer instanceof ServerPlayer serverSourcePlayer) {
            Packets.sendToOtherClients(new StopOnePerPlayerSoundS2CP(serverSourcePlayer.getUUID(), soundEvent),
                    serverSourcePlayer, serverPlayer -> serverSourcePlayer.distanceTo(serverPlayer) < soundEvent.getRange(1f));
        }
    }
}
