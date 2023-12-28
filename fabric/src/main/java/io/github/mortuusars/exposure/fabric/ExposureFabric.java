package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.command.ExposureCommand;
import io.github.mortuusars.exposure.command.ShaderCommand;
import io.github.mortuusars.exposure.command.TestCommand;
import io.github.mortuusars.exposure.command.argument.ShaderLocationArgument;
import io.github.mortuusars.exposure.fabric.integration.create.CreateFilmDeveloping;
import io.github.mortuusars.exposure.network.fabric.PacketsImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.api.fml.event.config.ModConfigEvents;
import net.minecraftforge.fml.config.ModConfig;

public class ExposureFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Exposure.init();

        ModConfigEvents.reloading(Exposure.ID).register(config -> {
            Config.reloading(config.getType());
            if (config.getType() == ModConfig.Type.COMMON && FabricLoader.getInstance().isModLoaded("create")) {
                CreateFilmDeveloping.clearCachedData();
            }
        });

        ModConfigEvents.loading(Exposure.ID).register(config -> {
            Config.loading(config.getType());
        });

        ModLoadingContext.registerConfig(Exposure.ID, ModConfig.Type.COMMON, Config.Common.SPEC);
        ModLoadingContext.registerConfig(Exposure.ID, ModConfig.Type.CLIENT, Config.Client.SPEC);

        ArgumentTypeRegistry.registerArgumentType(Exposure.resource("shader_location"),
                ShaderLocationArgument.class, SingletonArgumentInfo.contextFree(ShaderLocationArgument::new));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            ExposureCommand.register(dispatcher);
            ShaderCommand.register(dispatcher);
            TestCommand.register(dispatcher);
        });

        Exposure.Advancements.register();
        Exposure.Stats.register();

        ServerLifecycleEvents.SERVER_STARTING.register(Exposure::initServer);

        PacketsImpl.registerC2SPackets();
    }
}
