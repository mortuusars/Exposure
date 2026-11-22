package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.item.crafting.recipe.display.ComponentTransferringRecipeDisplay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ComponentTransferringRecipeDisplayResponseS2CP(Optional<ComponentTransferringRecipeDisplay> display, ItemStack stack) implements Packet {
    public static final Identifier ID = Exposure.resource("component_transferring_recipe_display_response");
    public static final Type<ComponentTransferringRecipeDisplayResponseS2CP> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ComponentTransferringRecipeDisplayResponseS2CP> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ComponentTransferringRecipeDisplay.STREAM_CODEC), ComponentTransferringRecipeDisplayResponseS2CP::display,
            ItemStack.STREAM_CODEC, ComponentTransferringRecipeDisplayResponseS2CP::stack,
            ComponentTransferringRecipeDisplayResponseS2CP::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientGUI.recipeDisplays.put(stack.getItem(), display);

        return true;
    }
}
