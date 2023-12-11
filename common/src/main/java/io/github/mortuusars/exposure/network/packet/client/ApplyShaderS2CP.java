package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ApplyShaderS2CP(ResourceLocation shaderLocation) implements IPacket<ApplyShaderS2CP> {
    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(shaderLocation);
    }

    public static ApplyShaderS2CP fromBuffer(FriendlyByteBuf buffer) {
        return new ApplyShaderS2CP(buffer.readResourceLocation());
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        ClientPacketsHandler.applyShader(this);
        return true;
    }
}
