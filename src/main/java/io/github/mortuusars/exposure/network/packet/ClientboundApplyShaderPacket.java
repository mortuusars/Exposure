package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundApplyShaderPacket(ResourceLocation shaderLocation) {
    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(shaderLocation);
    }

    public static ClientboundApplyShaderPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ClientboundApplyShaderPacket(buffer.readResourceLocation());
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
