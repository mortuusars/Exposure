package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.inventory.ItemRenameMenu;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleRenameItem", at = @At("HEAD"), cancellable = true)
    private void handleRename(ServerboundRenameItemPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl)(Object)this, player.level());
        if (player.containerMenu instanceof ItemRenameMenu itemRenameMenu) {
            if (!itemRenameMenu.stillValid(player)) {
                Exposure.LOGGER.debug("Player {} interacted with invalid menu {}", player, itemRenameMenu);
            }
            else {
                itemRenameMenu.setItemName(packet.getName());
            }

            ci.cancel();
        }
    }
}
