package io.github.mortuusars.exposure.fabric;

import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.command.ExposureCommands;
import io.github.mortuusars.exposure.command.ShaderCommand;
import io.github.mortuusars.exposure.command.TestCommand;
import io.github.mortuusars.exposure.command.argument.ShaderLocationArgument;
import io.github.mortuusars.exposure.fabric.integration.create.CreateFilmDeveloping;
import io.github.mortuusars.exposure.network.fabric.PacketsImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.world.item.CreativeModeTabs;
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

        ForgeConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.COMMON, Config.Common.SPEC);
        ForgeConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.CLIENT, Config.Client.SPEC);

        ArgumentTypeRegistry.registerArgumentType(Exposure.resource("shader_location"),
                ShaderLocationArgument.class, SingletonArgumentInfo.contextFree(ShaderLocationArgument::new));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            ExposureCommands.register(dispatcher);
            ShaderCommand.register(dispatcher);
            TestCommand.register(dispatcher);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.prepend(Exposure.Items.CAMERA.get());
            content.prepend(Exposure.Items.BLACK_AND_WHITE_FILM.get());
            content.prepend(Exposure.Items.COLOR_FILM.get());
            content.prepend(Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get());
            content.prepend(Exposure.Items.DEVELOPED_COLOR_FILM.get());
            content.prepend(Exposure.Items.PHOTOGRAPH.get());
            content.prepend(Exposure.Items.STACKED_PHOTOGRAPHS.get());
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            content.prepend(Exposure.Items.LIGHTROOM.get());
        });

        Exposure.Advancements.register();
        Exposure.Stats.register();

        ServerLifecycleEvents.SERVER_STARTING.register(Exposure::initServer);

        PacketsImpl.registerC2SPackets();
    }
}
