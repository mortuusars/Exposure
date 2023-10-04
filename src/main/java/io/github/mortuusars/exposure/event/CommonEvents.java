package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.command.ExposureCommands;
import io.github.mortuusars.exposure.command.ShaderCommand;
import io.github.mortuusars.exposure.command.argument.ShaderLocationArgument;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.ScheduledTasks;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonEvents {
    public static class ModBus {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(Packets::register);
        }
    }

    public static class ForgeBus {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            ArgumentTypeInfos.registerByClass(ShaderLocationArgument.class, SingletonArgumentInfo.contextFree(ShaderLocationArgument::new));

            ExposureCommands.register(event.getDispatcher());
            ShaderCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void playerTick(TickEvent.PlayerTickEvent event) {
            // Refresh active camera
            if (CameraInHand.isActive(event.player)) {
                CameraInHand camera = CameraInHand.ofPlayer(event.player);
                camera.getItem().setActive(event.player, camera.getStack(), true);
            }
            else if (ViewfinderClient.isOpen())
                ViewfinderClient.close(event.player);
        }

        @SubscribeEvent
        public static void levelTick(TickEvent.LevelTickEvent event) {
            if (event.phase == TickEvent.Phase.END)
                ScheduledTasks.tick(event);
        }

        @SubscribeEvent
        public static void entityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
            Player player = event.getEntity();

            // Interacting with entity when trying to shoot is annoying
            if (CameraInHand.isActive(player)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                CameraInHand camera = CameraInHand.ofPlayer(player);
                camera.getStack().use(player.level, player, camera.getHand());
            }
        }

        @SubscribeEvent
        public static void onItemToss(ItemTossEvent event) {
            ItemStack itemStack = event.getEntity().getItem();
            if (itemStack.getItem() instanceof CameraItem cameraItem)
                cameraItem.setActive(event.getPlayer(), itemStack, false);
        }
    }
}
