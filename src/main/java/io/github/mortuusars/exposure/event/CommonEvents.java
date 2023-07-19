package io.github.mortuusars.exposure.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.command.ExposureCommands;
import io.github.mortuusars.exposure.command.ShaderCommand;
import io.github.mortuusars.exposure.command.argument.ShaderLocationArgument;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.util.CameraInHand;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
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
            Exposure.getCamera().tick(event.player);
        }

        @SubscribeEvent
        public static void entityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
            Player player = event.getEntity();
            if (Exposure.getCamera().isActive(player)) { // TODO: Config whether to block entity interact
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                CameraInHand camera = Exposure.getCamera().getCameraInHand(player);
                camera.getItem().use(player.level, player, camera.getHand());
            }
        }
    }
}
