package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.PacketDirection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface IPacket<T> {
    void toBuffer(FriendlyByteBuf buffer);
    boolean handle(PacketDirection direction, @Nullable Player player);
}
