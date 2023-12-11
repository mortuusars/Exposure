package io.github.mortuusars.exposure.network.packet.client;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.sound.OnePerPlayerSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record StopOnePerPlayerSoundS2CP(UUID sourcePlayerId, SoundEvent soundEvent) implements IPacket<StopOnePerPlayerSoundS2CP> {
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUUID(sourcePlayerId);
        buffer.writeResourceLocation(soundEvent.getLocation());
    }

    public static StopOnePerPlayerSoundS2CP fromBuffer(FriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        ResourceLocation soundEventLocation = buffer.readResourceLocation();
        @Nullable SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundEventLocation);
        if (soundEvent == null)
            soundEvent = SoundEvents.NOTE_BLOCK_BASS.value();

        return new StopOnePerPlayerSoundS2CP(uuid, soundEvent);
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        if (Minecraft.getInstance().level != null) {
            @Nullable Player sourcePlayer = Minecraft.getInstance().level.getPlayerByUUID(sourcePlayerId);
            if (sourcePlayer != null)
                OnePerPlayerSounds.stop(sourcePlayer, soundEvent);
            else
                LogUtils.getLogger().debug("Cannot stop OnePerPlayer sound. SourcePlayer was not found by it's UUID.");
        }

        return true;
    }
}
