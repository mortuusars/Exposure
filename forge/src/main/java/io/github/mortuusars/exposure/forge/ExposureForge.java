package io.github.mortuusars.exposure.forge;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.forge.event.ClientEvents;
import io.github.mortuusars.exposure.forge.event.CommonEvents;
import io.github.mortuusars.exposure.forge.integration.create.CreateFilmDeveloping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Exposure.ID)
public class ExposureForge {
    public ExposureForge() {
        Exposure.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.Common.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onConfigLoading);
        modEventBus.addListener(this::onConfigReloading);

        RegisterImpl.BLOCKS.register(modEventBus);
        RegisterImpl.BLOCK_ENTITY_TYPES.register(modEventBus);
        RegisterImpl.ITEMS.register(modEventBus);
        RegisterImpl.ENTITY_TYPES.register(modEventBus);
        RegisterImpl.MENU_TYPES.register(modEventBus);
        RegisterImpl.RECIPE_SERIALIZERS.register(modEventBus);
        RegisterImpl.SOUND_EVENTS.register(modEventBus);

        modEventBus.register(CommonEvents.ModBus.class);
        MinecraftForge.EVENT_BUS.register(CommonEvents.ForgeBus.class);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.register(ClientEvents.ModBus.class);
            MinecraftForge.EVENT_BUS.register(ClientEvents.ForgeBus.class);
        });
    }

    private void onConfigLoading(ModConfigEvent.Loading event) {
        Config.loading(event.getConfig().getType());
        if (event.getConfig().getType() == ModConfig.Type.COMMON && ModList.get().isLoaded("create"))
            CreateFilmDeveloping.clearCachedData();
    }

    private void onConfigReloading(ModConfigEvent.Reloading event) {
        Config.reloading(event.getConfig().getType());
        if (event.getConfig().getType() == ModConfig.Type.COMMON && ModList.get().isLoaded("create"))
            CreateFilmDeveloping.clearCachedData();
    }
}
