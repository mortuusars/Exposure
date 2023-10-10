package io.github.mortuusars.exposure.client.sound;

import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShutterTimerTickingSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;
    private int delay = 2;

    public ShutterTimerTickingSoundInstance(Player player, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, RandomSource random) {
        super(soundEvent, soundSource, random);
        this.player = player;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.volume = volume;
        this.pitch = pitch;
        this.looping = true;
    }

    @Override
    public void tick() {
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();

        CameraInHand cameraInHand = new CameraInHand(player);
        if (cameraInHand.isEmpty() || !cameraInHand.getItem().isShutterOpen(cameraInHand.getStack(), player.getLevel())) {
            // In multiplayer other players camera stack is not updated in time (sometimes)
            // This causes the sound to stop instantly
            if (!player.equals(Minecraft.getInstance().player) && delay > 0) {
                delay--;
                return;
            }

            this.stop();
        }
    }
}
