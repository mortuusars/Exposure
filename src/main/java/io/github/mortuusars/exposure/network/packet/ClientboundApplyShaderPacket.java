package io.github.mortuusars.exposure.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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
    public boolean handle(Supplier<NetworkEvent.Context> ignoredContextSupplier) {
        Minecraft mc = Minecraft.getInstance();
        if (shaderLocation.getPath().equals("none")) {
            mc.gameRenderer.shutdownEffect();
        }
        else {
            mc.gameRenderer.loadEffect(shaderLocation);
        }

        return true;
    }
}
