package io.github.mortuusars.exposure.command;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.command.argument.ShaderLocationArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ModIdArgument;

@Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Commands {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ArgumentTypeInfos.registerByClass(ShaderLocationArgument.class, SingletonArgumentInfo.contextFree(ShaderLocationArgument::new));

        ShaderCommand.register(event.getDispatcher());
    }
}
