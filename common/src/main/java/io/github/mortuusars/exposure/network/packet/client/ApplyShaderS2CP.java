package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ApplyShaderS2CP(ResourceLocation shaderLocation) implements IPacket {
    public static final ResourceLocation ID = Exposure.resource("apply_shader");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FriendlyByteBuf toBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(shaderLocation);
        return buffer;
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
