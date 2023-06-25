package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.Photograph;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.network.ExposureSender;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import io.github.mortuusars.exposure.storage.ServersideExposureStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public record ServerboundPrintPhotographPacket(Photograph photographData) {
    public void toBuffer(FriendlyByteBuf buffer) {
        photographData.toBuffer(buffer);
    }

    public static ServerboundPrintPhotographPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundPrintPhotographPacket(Photograph.fromBuffer(buffer));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null)
            throw new IllegalStateException("Cannot handle PrintPhotographPacket: Player was null");

        int index = -1;

        for (ItemStack item : player.getInventory().items) {
            if (item.is(Items.PAPER)) {
                index = player.getInventory().findSlotMatchingItem(item);
            }
        }

        if (index == -1) {
            player.playNotifySound(SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 1f, 1f);
            player.displayClientMessage(Component.translatable("item.exposure.film.no_paper")
                    .withStyle(ChatFormatting.RED), true);
            return true;
        }

        player.getInventory().getItem(index).shrink(1);

//        player.getInventory().setItem(index, player.getInventory().getItem(index).shrink(1));

        PhotographItem photographItem = Exposure.Items.PHOTOGRAPH.get();
        ItemStack photographStack = new ItemStack(photographItem);
        photographItem.setPhotographData(photographStack, photographData);

        if (!player.addItem(photographStack))
            player.drop(photographStack, true, true);

        player.level.playSound(null, player, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1f);

        return true;
    }
}
