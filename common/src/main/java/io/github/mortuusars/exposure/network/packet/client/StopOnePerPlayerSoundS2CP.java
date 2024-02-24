package io.github.mortuusars.exposure.network.packet.client;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.sound.OnePerPlayerSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record StopOnePerPlayerSoundS2CP(UUID sourcePlayerId, SoundEvent soundEvent) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("stop_one_per_player_sound");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUUID(sourcePlayerId);
        buffer.writeResourceLocation(soundEvent.getLocation());
        return buffer;
    }

    public static StopOnePerPlayerSoundS2CP fromBuffer(FriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        ResourceLocation soundEventLocation = buffer.readResourceLocation();
        @Nullable SoundEvent soundEvent = Registry.SOUND_EVENT.get(soundEventLocation);
        if (soundEvent == null)
            soundEvent = SoundEvents.NOTE_BLOCK_BASS;

        return new StopOnePerPlayerSoundS2CP(uuid, soundEvent);
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        if (Minecraft.getInstance().level != null) {
            @Nullable Player sourcePlayer = Minecraft.getInstance().level.getPlayerByUUID(sourcePlayerId);
            if (sourcePlayer != null)
                Minecraft.getInstance().execute(() -> OnePerPlayerSounds.stop(sourcePlayer, soundEvent));
            else
                LogUtils.getLogger().debug("Cannot stop OnePerPlayer sound. SourcePlayer was not found by it's UUID.");
        }

        return true;
    }
}
