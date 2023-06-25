package io.github.mortuusars.exposure.camera.viewfinder;

import io.github.mortuusars.exposure.item.CameraItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ViewfinderClient implements IViewfinder {
    private final List<Player> activeCameras = new ArrayList<>();

    @Override
    public void activate(Player player) {
        activeCameras.add(player);
    }

    @Override
    public void deactivate(Player player) {
        activeCameras.remove(player);
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

    public void sendToServer(Player player) {

    }

    public record ServerboundUpdateViewfinderPacket(boolean isActive) {
        public void toBuffer(FriendlyByteBuf buffer) {
            buffer.writeBoolean(isActive);
        }

        public static ServerboundUpdateViewfinderPacket fromBuffer(FriendlyByteBuf buffer) {
            return new ServerboundUpdateViewfinderPacket(buffer.readBoolean());
        }

        @SuppressWarnings("UnusedReturnValue")
        public boolean handleServerside(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            ServerPlayer player = context.getSender();
            IViewfinder viewfinder = Viewfinder.get().getSidedViewfinder();

            if (isActive)
                viewfinder.activate(player);
            else
                viewfinder.deactivate(player);

            return true;
        }
    }
}
