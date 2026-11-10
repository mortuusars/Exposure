package io.github.mortuusars.exposure.world.sound;

import io.github.mortuusars.exposure.client.sound.UniqueSoundManager;
import io.github.mortuusars.mortaar.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.UniqueSoundPlayShutterTickingS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.UniqueSoundPlayS2CP;
import io.github.mortuusars.exposure.world.camera.CameraId;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class Sound {
    public static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource source) {
        play(level, x, y, z, sound ,source, 1F, 1F, 0F);
    }

    public static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch) {
        play(level, x, y, z, sound ,source, volume, pitch, 0F);
    }

    public static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, float pitchVariability) {
        pitch = vary(pitch, pitchVariability);
        level.playSound(null, x, y, z, sound, source, volume, pitch);
    }

    public static void play(Entity entity, SoundEvent sound) {
        play(entity, sound, entity.getSoundSource(), 1F, 1F, 0F);
    }

    public static void play(Entity entity, SoundEvent sound, SoundSource source) {
        play(entity, sound, source, 1F, 1F, 0F);
    }

    public static void play(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
        play(entity, sound, source, volume, pitch, 0F);
    }

    public static void play(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch, float pitchVariability) {
        pitch = vary(pitch, pitchVariability);
        entity.level().playSound(null, entity, sound, source, volume, pitch);
    }

    // --

    public static void playSided(Player player, double x, double y, double z, SoundEvent sound, SoundSource source) {
        playSided(player, x, y, z, sound, source, 1F, 1F, 0F);
    }

    public static void playSided(Player player, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch) {
        playSided(player, x, y, z, sound, source, volume, pitch, 0F);
    }

    public static void playSided(Player player, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, float pitchVariability) {
        pitch = vary(pitch, pitchVariability);
        player.level().playSound(player, x, y, z, sound, source, volume, pitch);
    }

    public static void playSided(Entity entity, SoundEffect sound) {
        playSided(entity, sound.get(), entity.getSoundSource(), sound.volume(), sound.pitch(), sound.pitchVariability());
    }

    public static void playSided(Entity entity, SoundEvent sound) {
        playSided(entity, sound, entity.getSoundSource());
    }

    public static void playSided(Entity entity, SoundEvent sound, SoundSource source) {
        playSided(entity, sound, source, 1F, 1F);
    }

    public static void playSided(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
        playSided(entity, sound, source, volume, pitch, 0F);
    }

    public static void playSided(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch, float pitchVariability) {
        pitch = vary(pitch, pitchVariability);
        @Nullable Player player = entity instanceof Player ? ((Player) entity) : null;
        entity.level().playSound(player, entity, sound, source, volume, pitch);
    }

    // --

    public static void playUnique(String id, Entity entity, SoundEffect sound, SoundSource source) {
        playUnique(id, entity, sound.get(), source, sound.volume(), sound.pitch(), sound.pitchVariability());
    }

    public static void playUnique(String id, Entity entity, SoundEvent sound, SoundSource source) {
        playUnique(id, entity, sound, source, 1F, 1F, 0F);
    }

    public static void playUnique(String id, Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
        playUnique(id, entity, sound, source, volume, pitch, 0F);
    }

    public static void playUnique(String id, Entity entity, SoundEvent sound, SoundSource source,
                                  float volume, float pitch, float pitchVariability) {
        pitch = vary(pitch, pitchVariability);
        if (entity.level() instanceof ServerLevel serverLevel) {
            UniqueSoundPlayS2CP packet = new UniqueSoundPlayS2CP(id, entity.getId(), sound, source, volume, pitch);
            Packets.sendToPlayersNear(packet, serverLevel, null, entity, sound.getRange(1f) * 4);
        }
    }

    public static void playUniqueSided(String id, Entity entity, SoundEffect sound, SoundSource source) {
        @Nullable ServerPlayer excludedPlayer = entity instanceof ServerPlayer player ? player : null;

        float pitch = vary(sound.pitch(), sound.pitchVariability());
        if (entity.level() instanceof ServerLevel serverLevel) {
            UniqueSoundPlayS2CP packet = new UniqueSoundPlayS2CP(id, entity.getId(), sound.get(), source, sound.volume(), sound.pitch());
            Packets.sendToPlayersNear(packet, serverLevel, excludedPlayer, entity, sound.get().getRange(1f) * 4);
        } else if (entity instanceof Player && entity.level().isClientSide()) {
            UniqueSoundManager.play(id, entity, sound.get(), source, sound.volume(), pitch);
        }
    }

    public static void playUniqueSided(String id, Player player, Entity entity, SoundEffect sound, SoundSource source) {
        playUniqueSided(id, player, entity, sound.get(), source, sound.volume(), sound.pitch(), sound.pitchVariability());
    }

    public static void playUniqueSided(String id, Player player, Entity entity, SoundEvent sound, SoundSource source,
                                       float volume, float pitch, float pitchVariability) {
        pitch = vary(pitch, pitchVariability);
        if (player.level() instanceof ServerLevel serverLevel) {
            UniqueSoundPlayS2CP packet = new UniqueSoundPlayS2CP(id, entity.getId(), sound, source, volume, pitch);
            Packets.sendToPlayersNear(packet, serverLevel, ((ServerPlayer) player), entity, sound.getRange(1f) * 4);
        } else if (player.level().isClientSide()) {
            UniqueSoundManager.play(id, entity, sound, source, volume, pitch);
        }
    }

    private static float vary(float value, float variability) {
        return value - (variability / 2) + ThreadLocalRandom.current().nextFloat() * variability;
    }

    public static void playShutterTicking(Entity entity, CameraId cameraId, int duration) {
        if (!entity.level().isClientSide()) {
            Packets.sendToAllClients(new UniqueSoundPlayShutterTickingS2CP(entity.getId(), cameraId, 1F, 1F, duration));
        }
    }
}
