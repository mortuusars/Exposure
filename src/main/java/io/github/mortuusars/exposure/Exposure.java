package io.github.mortuusars.exposure;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.block.DarkroomBlock;
import io.github.mortuusars.exposure.block.entity.DarkroomBlockEntity;
import io.github.mortuusars.exposure.camera.infrastructure.Camera;
import io.github.mortuusars.exposure.camera.ExposureCapture;
import io.github.mortuusars.exposure.camera.infrastructure.ClientCameraHolder;
import io.github.mortuusars.exposure.camera.infrastructure.ServerCameraHolder;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.client.render.ViewfinderRenderer;
import io.github.mortuusars.exposure.config.ClientConfig;
import io.github.mortuusars.exposure.event.ClientEvents;
import io.github.mortuusars.exposure.event.CommonEvents;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.item.CameraItem;
import io.github.mortuusars.exposure.item.FilmItem;
import io.github.mortuusars.exposure.menu.DarkroomMenu;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import io.github.mortuusars.exposure.storage.IExposureStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Exposure.ID)
public class Exposure {
    public static final String ID = "exposure";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Exposure() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Blocks.BLOCKS.register(modEventBus);
        BlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        MenuTypes.MENU_TYPES.register(modEventBus);

        modEventBus.register(CommonEvents.ModBus.class);
        MinecraftForge.EVENT_BUS.register(CommonEvents.ForgeBus.class);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.register(ClientEvents.ModBus.class);
            MinecraftForge.EVENT_BUS.register(ClientEvents.ForgeBus.class);

            MinecraftForge.EVENT_BUS.addListener(ViewfinderRenderer::onComputeFovEvent);
            MinecraftForge.EVENT_BUS.addListener(ViewfinderRenderer::onMouseScrollEvent);
            MinecraftForge.EVENT_BUS.addListener(ExposureCapture::onRenderTick);
        });
    }

    public static Camera getCamera() {
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER ?
                ServerCameraHolder.SERVER_CAMERA : ClientCameraHolder.CLIENT_CAMERA;
    }

    public static IExposureStorage getStorage() {
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER ?
                ExposureStorage.SERVER : ExposureStorage.CLIENT;
    }

    /**
     * Creates resource location in the mod namespace with the given path.
     */
    public static ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
    }

    public static class Blocks {
        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);

        public static final RegistryObject<DarkroomBlock> DARKROOM = BLOCKS.register("darkroom",
                () -> new DarkroomBlock(BlockBehaviour.Properties.of(Material.WOOD)
                        .color(MaterialColor.COLOR_BROWN)));
    }

    @SuppressWarnings("DataFlowIssue")
    public static class BlockEntityTypes {
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
                DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);

        public static final RegistryObject<BlockEntityType<DarkroomBlockEntity>> DARKROOM = BLOCK_ENTITY_TYPES.register("darkroom",
                () -> BlockEntityType.Builder.of(DarkroomBlockEntity::new, Blocks.DARKROOM.get()).build(null));
    }

    public static class Items {
        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);

        public static final RegistryObject<CameraItem> CAMERA = ITEMS.register("camera",
                () -> new CameraItem(new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<FilmItem> BLACK_AND_WHITE_FILM = ITEMS.register("black_and_white_film",
                () -> new FilmItem(FilmType.BLACK_AND_WHITE, 8, Mth.color(0.8F, 0.8F, 0.9F), new Item.Properties()
                        .stacksTo(16)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<FilmItem> COLOR_FILM = ITEMS.register("color_film",
                () -> new FilmItem(FilmType.COLOR, 16, Mth.color(0.4F, 0.4F, 1.0F), new Item.Properties()
                        .stacksTo(16)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<PhotographItem> PHOTOGRAPH = ITEMS.register("photograph",
                () -> new PhotographItem(new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<BlockItem> DARKROOM = ITEMS.register("darkroom",
                () -> new BlockItem(Blocks.DARKROOM.get(), new Item.Properties()
                        .tab(CreativeModeTab.TAB_DECORATIONS)));
    }

    public static class MenuTypes {
        private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ID);

        public static final RegistryObject<MenuType<CameraAttachmentsMenu>> CAMERA = MENU_TYPES
                .register("camera", () -> IForgeMenuType.create(CameraAttachmentsMenu::fromBuffer));

        public static final RegistryObject<MenuType<DarkroomMenu>> DARKROOM = MENU_TYPES
                .register("darkroom", () -> IForgeMenuType.create(DarkroomMenu::fromBuffer));
    }
}
