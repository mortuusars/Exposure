package io.github.mortuusars.exposure.network.packet.client;

import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public record ApplyShaderClientboundPacket(ResourceLocation shaderLocation) {
    public static void register(SimpleChannel channel, int id) {
        channel.messageBuilder(ApplyShaderClientboundPacket.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ApplyShaderClientboundPacket::toBuffer)
                .decoder(ApplyShaderClientboundPacket::fromBuffer)
                .consumerMainThread(ApplyShaderClientboundPacket::handle)
                .add();
    }

    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(shaderLocation);
    }

    public static ApplyShaderClientboundPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ApplyShaderClientboundPacket(buffer.readResourceLocation());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            contextSupplier.get().enqueueWork(() -> {
                ClientPacketsHandler.applyShader(this);
            });
        });

        return true;
    }
}
