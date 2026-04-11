package io.github.mortuusars.exposure.client.sound;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class UniqueSoundManager {
    private static final Table<String, Identifier, SoundInstance> SOUNDS = HashBasedTable.create();

    public static void play(String id, SoundInstance instance) {
        stop(id, instance.getIdentifier());

        SOUNDS.put(id, instance.getIdentifier(), instance);
        Minecrft.get().getSoundManager().play(instance);
    }

    public static void play(String id, Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
        SoundInstance soundInstance = createEntityBoundInstance(entity, sound, source, volume, pitch);
        play(id, soundInstance);
    }

    public static void play(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
        String id = entity.getScoreboardName();
        play(id, entity, sound, source, volume, pitch);
    }

    public static void stop(String id, Identifier location) {
        @Nullable SoundInstance instance = SOUNDS.remove(id, location);
        if (instance != null) Minecrft.get().getSoundManager().stop(instance);
    }

    public static void stop(String id, SoundEvent sound) {
        stop(id, sound.location());
    }

    public static void stopAllOf(SoundEvent sound) {
        Identifier location = sound.location();
        SOUNDS.cellSet().removeIf(cell -> {
            if (cell.getColumnKey().equals(location)) {
                Minecrft.get().getSoundManager().stop(cell.getValue());
                return true;
            }
            return false;
        });
    }

//    public static void update(String id, SoundEvent sound, Consumer<SoundInstance> updater) {
//        @Nullable SoundInstance instance = SOUNDS.get(id, sound.getLocation());
//        if (instance != null) {
//            updater.accept(instance);
//        }
//    }

    private static SoundInstance createEntityBoundInstance(Entity entity, SoundEvent soundEvent, SoundSource source,
                                                           float volume, float pitch) {
        return new EntityBoundSoundInstance(soundEvent, source, volume, pitch, entity, entity.getRandom().nextLong());
    }
}
