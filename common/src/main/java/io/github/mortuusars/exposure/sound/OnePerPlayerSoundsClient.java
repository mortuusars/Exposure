package io.github.mortuusars.exposure.sound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.sound.instance.ShutterTimerTickingSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OnePerPlayerSoundsClient {
    private static final Map<Player, List<SoundInstance>> instances = new HashMap<>();

    public static void play(Player sourcePlayer, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        Level level = sourcePlayer.level();
        stop(sourcePlayer, soundEvent);

        SoundInstance soundInstance = createSoundInstance(sourcePlayer, soundEvent, source, volume, pitch, level);

        List<SoundInstance> playingSounds = Optional.ofNullable(instances.get(sourcePlayer)).orElse(new ArrayList<>());
        playingSounds.add(soundInstance);
        instances.put(sourcePlayer, playingSounds);

        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }

    public static void stop(Player sourcePlayer, SoundEvent soundEvent) {
        if (instances.containsKey(sourcePlayer)) {
            ResourceLocation soundLocation = soundEvent.getLocation();
            List<SoundInstance> playingSounds = instances.remove(sourcePlayer);
            for (int i = playingSounds.size() - 1; i >= 0; i--) {
                SoundInstance soundInstance = playingSounds.get(i);
                if (soundInstance.getLocation().equals(soundLocation)) {
                    Minecraft.getInstance().getSoundManager().stop(soundInstance);
                    playingSounds.remove(i);
                }
            }

            instances.put(sourcePlayer, playingSounds);
        }
    }


    @NotNull
    private static SoundInstance createSoundInstance(Player sourcePlayer, SoundEvent soundEvent, SoundSource source, float volume, float pitch, Level level) {
        if (soundEvent == Exposure.SoundEvents.SHUTTER_TICKING.get())
            return new ShutterTimerTickingSoundInstance(sourcePlayer, soundEvent, source, volume, pitch, sourcePlayer.level().getRandom());

        return new EntityBoundSoundInstance(soundEvent, source, volume, pitch, sourcePlayer, level.getRandom().nextLong());
    }
}
