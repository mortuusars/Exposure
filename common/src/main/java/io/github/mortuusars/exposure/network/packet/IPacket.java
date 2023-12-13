package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.PacketDirection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface IPacket<T> {
    FriendlyByteBuf toBuffer(FriendlyByteBuf buffer);
    /**
     * @param direction
     * @param player will be null when on the client.
     * @return
     */
    boolean handle(PacketDirection direction, @Nullable Player player);
    ResourceLocation getId();
}
