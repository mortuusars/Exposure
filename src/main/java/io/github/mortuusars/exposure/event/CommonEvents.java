package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.camera.CameraHelper;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.command.ExposureCommands;
import io.github.mortuusars.exposure.command.ShaderCommand;
import io.github.mortuusars.exposure.command.TestCommand;
import io.github.mortuusars.exposure.command.argument.ShaderLocationArgument;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.util.CameraInHand;
import io.github.mortuusars.exposure.util.ScheduledTasks;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonEvents {
    public static class ModBus {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                Packets.register();
                Exposure.Advancements.register();
                Exposure.Stats.register();
            });
        }
    }

    public static class ForgeBus {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            ArgumentTypeInfos.registerByClass(ShaderLocationArgument.class, SingletonArgumentInfo.contextFree(ShaderLocationArgument::new));

            ExposureCommands.register(event.getDispatcher());
            ShaderCommand.register(event.getDispatcher());
            TestCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void playerTick(TickEvent.PlayerTickEvent event) {
            Player player = event.player;
            if (event.phase != TickEvent.Phase.END || !player.getLevel().isClientSide || !player.equals(Minecraft.getInstance().player))
                return;

            ViewfinderClient.onPlayerTick(player);
        }

        // IDK why but LevelTickEvent is fired 3 times on the server per 1 on the client.
        // So the solution is to use specific events. This seems to work properly.
        @SubscribeEvent
        public static void serverTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END)
                ScheduledTasks.tick(event);
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END)
                ScheduledTasks.tick(event);
        }

        @SubscribeEvent
        public static void entityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
            Player player = event.getEntity();

            // Interacting with entity when trying to shoot is annoying
            CameraInHand camera = CameraInHand.getActive(player);
            if (!camera.isEmpty()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                camera.getStack().use(player.level, player, camera.getHand());
            }
        }

        @SubscribeEvent
        public static void onItemToss(ItemTossEvent event) {
            ItemStack stack = event.getEntity().getItem();
            if (stack.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(stack))
                cameraItem.deactivate(event.getPlayer(), stack);
        }
    }
}
