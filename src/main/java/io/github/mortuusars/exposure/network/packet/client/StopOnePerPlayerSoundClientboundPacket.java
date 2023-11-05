package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.sound.OnePerPlayerSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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

public record StopOnePerPlayerSoundClientboundPacket(UUID sourcePlayerId, SoundEvent soundEvent) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(StopOnePerPlayerSoundClientboundPacket.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StopOnePerPlayerSoundClientboundPacket::toBuffer)
                .decoder(StopOnePerPlayerSoundClientboundPacket::fromBuffer)
                .consumerMainThread(StopOnePerPlayerSoundClientboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUUID(sourcePlayerId);
        buffer.writeResourceLocation(soundEvent.getLocation());
    }

    public static StopOnePerPlayerSoundClientboundPacket fromBuffer(FriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        ResourceLocation soundEventLocation = buffer.readResourceLocation();
        @Nullable SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(soundEventLocation);
        if (soundEvent == null)
            soundEvent = SoundEvents.NOTE_BLOCK_BASS;

        return new StopOnePerPlayerSoundClientboundPacket(uuid, soundEvent);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier< NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> contextSupplier.get().enqueueWork(
                () -> {
                    if (Minecraft.getInstance().level != null) {
                        @Nullable Player player = Minecraft.getInstance().level.getPlayerByUUID(sourcePlayerId);
                        if (player != null)
                            OnePerPlayerSounds.stop(player, soundEvent);
                        else
                            Exposure.LOGGER.debug("Cannot stop OnePerPlayer sound. SourcePlayer was not found by it's UUID.");
                    }
                }));

        return true;
    }
}
