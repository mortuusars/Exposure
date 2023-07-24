package io.github.mortuusars.exposure.client;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShutterOpenSoundInstance extends AbstractTickableSoundInstance {
    protected ShutterOpenSoundInstance(SoundEvent p_235076_, SoundSource p_235077_, RandomSource p_235078_) {
        super(p_235076_, p_235077_, p_235078_);
    }

    @Override
    public void tick() {

    }
}
