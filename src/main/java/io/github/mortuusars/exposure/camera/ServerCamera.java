package io.github.mortuusars.exposure.camera;

import io.github.mortuusars.exposure.camera.component.Shutter;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.UpdateActiveCameraPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ServerCamera extends Camera {
    public ServerCamera(Shutter shutter) {
        super(shutter);
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void activate(Player player, InteractionHand hand) {
        super.activate(player, hand);
        updateOtherClients(player, true, hand);
    }

    @Override
    public @Nullable InteractionHand deactivate(Player player) {
        InteractionHand hand = super.deactivate(player);
        if (hand != null)
            updateOtherClients(player, false, hand);
        return hand;
    }
    private void updateOtherClients(@Nullable Player player, boolean isActive, InteractionHand hand) {
        List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
        for (ServerPlayer serverPlayer : players) {
            if (!serverPlayer.equals(player))
                Packets.sendToClient(new UpdateActiveCameraPacket(serverPlayer.getUUID(), isActive, hand), serverPlayer);
        }
    }
}
