package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.sound.OnePerPlayerSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public record PlayOnePerPlayerSoundClientboundPacket(UUID sourcePlayerId, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(PlayOnePerPlayerSoundClientboundPacket.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayOnePerPlayerSoundClientboundPacket::toBuffer)
                .decoder(PlayOnePerPlayerSoundClientboundPacket::fromBuffer)
                .consumerMainThread(PlayOnePerPlayerSoundClientboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUUID(sourcePlayerId);
        buffer.writeResourceLocation(soundEvent.getLocation());
        buffer.writeEnum(source);
        buffer.writeFloat(volume);
        buffer.writeFloat(pitch);
    }

    public static PlayOnePerPlayerSoundClientboundPacket fromBuffer(FriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        ResourceLocation soundEventLocation = buffer.readResourceLocation();
        @Nullable SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(soundEventLocation);
        if (soundEvent == null)
            soundEvent = SoundEvents.NOTE_BLOCK_BASS;

        return new PlayOnePerPlayerSoundClientboundPacket(uuid, soundEvent, buffer.readEnum(SoundSource.class),
                buffer.readFloat(), buffer.readFloat());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier< NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> contextSupplier.get().enqueueWork(
                () -> {
                    if (Minecraft.getInstance().level != null) {
                        @Nullable Player player = Minecraft.getInstance().level.getPlayerByUUID(sourcePlayerId);
                        if (player != null)
                            OnePerPlayerSounds.play(player, soundEvent, source, volume, pitch);
                        else
                            Exposure.LOGGER.debug("Cannot play OnePerPlayer sound. SourcePlayer was not found by it's UUID.");
                    }
                }));

        return true;
    }
}
