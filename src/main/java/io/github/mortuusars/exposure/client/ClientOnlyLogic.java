package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.SyncCameraServerboundPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ClientOnlyLogic {
    private static final Map<Player, SoundInstance> filmAdvanceSoundInstances = new HashMap<>();

    public static void playCancellableFilmAdvanceSound(Player player) {
        if (filmAdvanceSoundInstances.containsKey(player)) {
            Minecraft.getInstance().getSoundManager().stop(filmAdvanceSoundInstances.get(player));
        }

        RandomSource random = player.level.getRandom();

        FilmAdvanceSoundInstance instance = new FilmAdvanceSoundInstance(player, Exposure.SoundEvents.FILM_ADVANCE.get(),
                SoundSource.PLAYERS, random);
        filmAdvanceSoundInstances.put(player, instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }
}
