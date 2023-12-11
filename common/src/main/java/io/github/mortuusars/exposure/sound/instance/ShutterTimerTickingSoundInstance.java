package io.github.mortuusars.exposure.sound.instance;

import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShutterTimerTickingSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;
    private int delay = 2;
    private final float originalVolume;
    public ShutterTimerTickingSoundInstance(Player player, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, RandomSource random) {
        super(soundEvent, soundSource, random);
        this.player = player;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.volume = volume;
        this.originalVolume = volume;
        this.pitch = pitch;
        this.looping = true;
    }

    @Override
    public void tick() {
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();

        if (hasShutterOpen(player.getMainHandItem()) || hasShutterOpen(player.getOffhandItem())) {
            volume = Mth.lerp(0.3f, volume, originalVolume);
            return;
        }
        else
            volume = Mth.lerp(0.2f, volume, originalVolume * 0.3f);

        if (!hasCameraWithOpenShutterInInventory(player)) {
            // In multiplayer other players camera stack is not updated in time (sometimes)
            // This causes the sound to stop instantly
            if (!player.equals(Minecraft.getInstance().player) && delay > 0) {
                delay--;
                return;
            }

            this.stop();
        }
    }

    private boolean hasCameraWithOpenShutterInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (hasShutterOpen(stack))
                return true;
        }

        return false;
    }

    private boolean hasShutterOpen(ItemStack stack) {
        return stack.getItem() instanceof CameraItem cameraItem && cameraItem.isShutterOpen(stack);
    }
}
