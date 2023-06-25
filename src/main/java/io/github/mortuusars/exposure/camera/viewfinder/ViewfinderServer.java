package io.github.mortuusars.exposure.camera.viewfinder;

import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ViewfinderServer implements IViewfinder {
    private final List<Player> activeCameras = new ArrayList<>();

    @Override
    public void activate(Player player) {
        activeCameras.add(player);
        broadcastToOtherPlayers(player);
    }

    @Override
    public void deactivate(Player player) {
        activeCameras.remove(player);
        broadcastToOtherPlayers(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activeCameras.contains(player);
    }

    @Override
    public void update() {
        List<Player> inactive = new ArrayList<>();
        for (Player player : activeCameras) {
            if (!(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof CameraItem) &&
                    !(player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof CameraItem)) {
                inactive.add(player);
            }
        }

        for (Player player : inactive) {
            activeCameras.remove(player);
        }
    }

    public void broadcastToOtherPlayers(@Nullable Player sourcePlayer) {
        update();
        List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            if (!player.equals(sourcePlayer))
                Packets.sendToClient(new ClientboundUpdateViewfinderPacket(activeCameras.contains(player)), player);
        }
    }

    public record ClientboundUpdateViewfinderPacket(boolean isActive) {
        public void toBuffer(FriendlyByteBuf buffer) {
            buffer.writeBoolean(isActive);
        }

        public static ClientboundUpdateViewfinderPacket fromBuffer(FriendlyByteBuf buffer) {
            return new ClientboundUpdateViewfinderPacket(buffer.readBoolean());
        }

        @SuppressWarnings("UnusedReturnValue")
        public boolean handleClientside(Supplier<NetworkEvent.Context> ignoredContextSupplier) {
            IViewfinder viewfinder = Viewfinder.get().getSidedViewfinder();

            LocalPlayer player = Minecraft.getInstance().player;

            if (isActive)
                viewfinder.activate(player);
            else
                viewfinder.deactivate(player);

            return true;
        }
    }
}
