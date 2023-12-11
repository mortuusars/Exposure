package io.github.mortuusars.exposure.network.packet.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.PacketDirection;
import io.github.mortuusars.exposure.network.packet.IPacket;
import io.github.mortuusars.exposure.util.ItemAndStack;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record CameraInHandAddFrameC2SP(InteractionHand hand, CompoundTag frame) implements IPacket<CameraInHandAddFrameC2SP> {
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeNbt(frame);
    }

    public static CameraInHandAddFrameC2SP fromBuffer(FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readEnum(InteractionHand.class);
        @Nullable CompoundTag frame = buffer.readAnySizeNbt();
        if (frame == null)
            frame = new CompoundTag();
        return new CameraInHandAddFrameC2SP(hand, frame);
    }

    @Override
    public boolean handle(PacketDirection direction, @Nullable Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");
        ServerPlayer serverPlayer = ((ServerPlayer) player);

        ItemStack itemInHand = player.getItemInHand(hand);
        if (!(itemInHand.getItem() instanceof CameraItem cameraItem))
            throw new IllegalStateException("Item in hand in not a Camera.");

        addStructuresInfo(serverPlayer);

        cameraItem.exposeFilmFrame(itemInHand, frame);
        Exposure.Advancements.FILM_FRAME_EXPOSED.trigger(serverPlayer, new ItemAndStack<>(itemInHand), frame);
        return true;
    }

    private void addStructuresInfo(@NotNull ServerPlayer player) {
        Map<Structure, LongSet> allStructuresAt = player.serverLevel().structureManager().getAllStructuresAt(player.blockPosition());

        List<Structure> inside = new ArrayList<>();

        for (Structure structure : allStructuresAt.keySet()) {
            StructureStart structureAt = player.serverLevel().structureManager().getStructureAt(player.blockPosition(), structure);
            if (structureAt.isValid()) {
                inside.add(structure);
            }
        }

        Registry<Structure> structures = player.serverLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
        ListTag structuresTag = new ListTag();

        for (Structure structure : inside) {
            ResourceLocation key = structures.getKey(structure);
            if (key != null)
                structuresTag.add(StringTag.valueOf(key.toString()));
        }

        if (structuresTag.size() > 0) {
            frame.put("Structures", structuresTag);
        }
    }
}
