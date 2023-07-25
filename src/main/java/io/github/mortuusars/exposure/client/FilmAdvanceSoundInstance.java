package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FilmAdvanceSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    protected FilmAdvanceSoundInstance(Player player, SoundEvent soundEvent, SoundSource soundSource, RandomSource random) {
        super(soundEvent, soundSource, random);
        this.player = player;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.volume = 1f;
        this.pitch = random.nextFloat() * 0.15f + 0.93f;
    }

    @Override
    public void tick() {
        if (Exposure.getCamera().getCameraInHand(player).isEmpty())
            this.stop();

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }
}
