package io.github.mortuusars.exposure.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record ServerboundTakeImagePacket(CompoundTag mapDataTag, String id) {

    public void toBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(mapDataTag);
        friendlyByteBuf.writeUtf(id);
    }

    public static ServerboundTakeImagePacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundTakeImagePacket(buffer.readNbt(), buffer.readUtf());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null)
            throw new IllegalStateException("Player was null");

        Level level = player.getLevel();

        MapItemSavedData mapData = MapItemSavedData.load(mapDataTag);

        level.setMapData(id, mapData);

//        ItemStack mapStack = new ItemStack(Items.FILLED_MAP);
//        storeMapData(mapStack, id);
//        player.drop(mapStack, false);

        player.level.setBlockAndUpdate(player.blockPosition().above(), Blocks.AIR.defaultBlockState());

        return true;
    }

    private static void storeMapData(ItemStack pStack, int pMapId) {
        pStack.getOrCreateTag().putInt("map", pMapId);
        pStack.getOrCreateTag().putBoolean("locked", true);
    }
}
