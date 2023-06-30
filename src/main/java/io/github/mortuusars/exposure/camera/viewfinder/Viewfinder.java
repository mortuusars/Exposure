package io.github.mortuusars.exposure.camera.viewfinder;

import io.github.mortuusars.exposure.client.ViewfinderRenderer;
import io.github.mortuusars.exposure.network.Packets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Viewfinder implements IViewfinder {
    private static final Map<Player, InteractionHand> playersWithActiveViewfinder = new HashMap<>();

    @Override
    public void activate(Player player, InteractionHand hand) {
        playersWithActiveViewfinder.put(player, hand);
        ViewfinderRenderer.setup(player, hand);
        broadcast(player, true, hand);
    }

    @Override
    public void deactivate(Player player) {
        @Nullable InteractionHand hand = playersWithActiveViewfinder.remove(player);
        if (hand != null)
            broadcast(player, false, hand);
    }

    @Override
    public boolean isActive(Player player) {
        return playersWithActiveViewfinder.containsKey(player); //TODO: check for camera in hand?
    }

    @Override
    public InteractionHand getActiveHand(Player player) {
        @Nullable InteractionHand activeHand = playersWithActiveViewfinder.get(player);
        return activeHand != null ? activeHand : InteractionHand.MAIN_HAND;
    }

    private void broadcast(Player player, boolean isActive, InteractionHand hand) {
        if (player.getLevel().isClientSide)
            Packets.sendToServer(new UpdateViewfinderPacket(isActive, hand));
    }

    public record UpdateViewfinderPacket(boolean isActive, InteractionHand hand) {
        public void toBuffer(FriendlyByteBuf buffer) {
            buffer.writeBoolean(isActive);
            buffer.writeEnum(hand);
        }

        public static UpdateViewfinderPacket fromBuffer(FriendlyByteBuf buffer) {
            return new UpdateViewfinderPacket(buffer.readBoolean(), buffer.readEnum(InteractionHand.class));
        }

        public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();

            if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
                handleServerside(context.getSender());
            else
                handleClientSide();

            return true;
        }

        private void handleClientSide() {
            if (isActive)
                playersWithActiveViewfinder.put(Minecraft.getInstance().player, hand);
            else
                playersWithActiveViewfinder.remove(Minecraft.getInstance().player);
        }

        private void handleServerside(@Nullable ServerPlayer sourcePlayer) {
            List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
            for (ServerPlayer player : players) {
                if (!player.equals(sourcePlayer))
                    Packets.sendToClient(new UpdateViewfinderPacket(isActive, hand), player);
            }
        }
    }
}
