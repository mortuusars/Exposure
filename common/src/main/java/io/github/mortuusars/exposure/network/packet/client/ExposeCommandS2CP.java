package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ExposeCommandS2CP(int size) implements IPacket<ExposeCommandS2CP> {
    public static final ResourceLocation ID = Exposure.resource("expose_command");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(size);
        return buffer;
    }

    public static ExposeCommandS2CP fromBuffer(FriendlyByteBuf buffer) {
        return new ExposeCommandS2CP(buffer.readInt());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.exposeScreenshot(size);
        return true;
    }
}
