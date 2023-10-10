package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.block.LightroomBlock;
import io.github.mortuusars.exposure.block.entity.FlashBlockEntity;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.camera.capture.CaptureManager;
import io.github.mortuusars.exposure.camera.film.FilmType;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderClient;
import io.github.mortuusars.exposure.camera.viewfinder.ViewfinderOverlay;
import io.github.mortuusars.exposure.config.Config;
import io.github.mortuusars.exposure.entity.PhotographEntity;
import io.github.mortuusars.exposure.event.ClientEvents;
import io.github.mortuusars.exposure.event.CommonEvents;
import io.github.mortuusars.exposure.item.*;
import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.recipe.FilmDevelopingRecipe;
import io.github.mortuusars.exposure.storage.ExposureStorage;
import io.github.mortuusars.exposure.storage.IExposureStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
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

    public static final int DEFAULT_FILM_SIZE = 320;
    public static final float CROP_FACTOR = 1.142857f;

    public static final String EXPOSURES_FOLDER_NAME = "exposures";

    public Exposure() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.Common.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Blocks.BLOCKS.register(modEventBus);
        BlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        EntityTypes.ENTITY_TYPES.register(modEventBus);
        MenuTypes.MENU_TYPES.register(modEventBus);
        RecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        SoundEvents.SOUNDS.register(modEventBus);

        modEventBus.register(CommonEvents.ModBus.class);
        MinecraftForge.EVENT_BUS.register(CommonEvents.ForgeBus.class);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.register(ClientEvents.ModBus.class);
            MinecraftForge.EVENT_BUS.register(ClientEvents.ForgeBus.class);
            MinecraftForge.EVENT_BUS.register(ViewfinderClient.ForgeEvents.class);

            MinecraftForge.EVENT_BUS.addListener(CaptureManager::onRenderTick);
        });
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

        public static final RegistryObject<LightroomBlock> LIGHTROOM = BLOCKS.register("lightroom",
                () -> new LightroomBlock(BlockBehaviour.Properties.of(Material.WOOD)
                        .color(MaterialColor.COLOR_BROWN)
                        .lightLevel(state -> 15)));

        public static final RegistryObject<FlashBlock> FLASH = BLOCKS.register("flash",
                () -> new FlashBlock(BlockBehaviour.Properties.of(Material.AIR)
                        .strength(-1.0F, 3600000.8F)
                        .noLootTable()
                        .color(MaterialColor.NONE)
                        .noOcclusion()
                        .noCollission()
                        .lightLevel(state -> 15)));
    }

    @SuppressWarnings("DataFlowIssue")
    public static class BlockEntityTypes {
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
                DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);

        public static final RegistryObject<BlockEntityType<LightroomBlockEntity>> LIGHTROOM = BLOCK_ENTITY_TYPES.register("lightroom",
                () -> BlockEntityType.Builder.of(LightroomBlockEntity::new, Blocks.LIGHTROOM.get()).build(null));
        public static final RegistryObject<BlockEntityType<FlashBlockEntity>> FLASH = BLOCK_ENTITY_TYPES.register("flash",
                () -> BlockEntityType.Builder.of(FlashBlockEntity::new, Blocks.FLASH.get()).build(null));
    }

    public static class Items {
        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);

        public static final RegistryObject<CameraItem> CAMERA = ITEMS.register("camera",
                () -> new CameraItem(new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<FilmRollItem> BLACK_AND_WHITE_FILM = ITEMS.register("black_and_white_film",
                () -> new FilmRollItem(FilmType.BLACK_AND_WHITE, DEFAULT_FILM_SIZE, Mth.color(0.8F, 0.8F, 0.9F), new Item.Properties()
                        .stacksTo(16)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<FilmRollItem> COLOR_FILM = ITEMS.register("color_film",
                () -> new FilmRollItem(FilmType.COLOR, DEFAULT_FILM_SIZE, Mth.color(0.4F, 0.4F, 1.0F), new Item.Properties()
                        .stacksTo(16)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<DevelopedFilmItem> DEVELOPED_BLACK_AND_WHITE_FILM = ITEMS.register("developed_black_and_white_film",
                () -> new DevelopedFilmItem(FilmType.BLACK_AND_WHITE, new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<DevelopedFilmItem> DEVELOPED_COLOR_FILM = ITEMS.register("developed_color_film",
                () -> new DevelopedFilmItem(FilmType.COLOR, new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<PhotographItem> PHOTOGRAPH = ITEMS.register("photograph",
                () -> new PhotographItem(new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<StackedPhotographsItem> STACKED_PHOTOGRAPHS = ITEMS.register("stacked_photographs",
                () -> new StackedPhotographsItem(16, new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final RegistryObject<BlockItem> LIGHTROOM = ITEMS.register("lightroom",
                () -> new BlockItem(Blocks.LIGHTROOM.get(), new Item.Properties()
                        .tab(CreativeModeTab.TAB_DECORATIONS)));
    }

    public static class EntityTypes {
        private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ID);
        public static final RegistryObject<EntityType<PhotographEntity>> PHOTOGRAPH = ENTITY_TYPES
                .register("photograph", () -> EntityType.Builder.<PhotographEntity>of(PhotographEntity::new, MobCategory.MISC)
                        .sized(0.5F, 0.5F)
                        .clientTrackingRange(10)
                        .updateInterval(Integer.MAX_VALUE)
                        .build("photograph"));
    }

    public static class MenuTypes {
        private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ID);

        public static final RegistryObject<MenuType<CameraAttachmentsMenu>> CAMERA = MENU_TYPES
                .register("camera", () -> IForgeMenuType.create(CameraAttachmentsMenu::fromBuffer));

        public static final RegistryObject<MenuType<LightroomMenu>> LIGHTROOM = MENU_TYPES
                .register("lightroom", () -> IForgeMenuType.create(LightroomMenu::fromBuffer));
    }

    public static class RecipeSerializers {
        private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
                DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Exposure.ID);

        public static final RegistryObject<RecipeSerializer<?>> FILM_DEVELOPING = RECIPE_SERIALIZERS.register("film_developing",
                FilmDevelopingRecipe.Serializer::new);
    }

    public static class SoundEvents {
        private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Exposure.ID);

        public static final RegistryObject<SoundEvent> VIEWFINDER_OPEN = register("item", "camera.viewfinder_open");
        public static final RegistryObject<SoundEvent> VIEWFINDER_CLOSE = register("item", "camera.viewfinder_close");
        public static final RegistryObject<SoundEvent> SHUTTER_OPEN = register("item", "camera.shutter_open");
        public static final RegistryObject<SoundEvent> SHUTTER_CLOSE = register("item", "camera.shutter_close");
        public static final RegistryObject<SoundEvent> SHUTTER_TICKING = register("item", "camera.shutter_ticking");
        public static final RegistryObject<SoundEvent> FILM_ADVANCE = register("item", "camera.film_advance");
        public static final RegistryObject<SoundEvent> CAMERA_BUTTON_CLICK = register("item", "camera.button_click");
        public static final RegistryObject<SoundEvent> CAMERA_DIAL_CLICK = register("item", "camera.dial_click");
        public static final RegistryObject<SoundEvent> CAMERA_LENS_RING_CLICK = register("item", "camera.lens_ring_click");
        public static final RegistryObject<SoundEvent> FILTER_PLACE = register("item", "camera.filter_place");
        public static final RegistryObject<SoundEvent> FLASH = register("item", "camera.flash");

        public static final RegistryObject<SoundEvent> PHOTOGRAPH_PLACE = register("item", "photograph.place");
        public static final RegistryObject<SoundEvent> PHOTOGRAPH_BREAK = register("item", "photograph.break");
        public static final RegistryObject<SoundEvent> PHOTOGRAPH_RUSTLE = register("item", "photograph.rustle");

        private static RegistryObject<SoundEvent> register(String category, String key) {
            Preconditions.checkState(category != null && category.length() > 0, "'category' should not be empty.");
            Preconditions.checkState(key != null && key.length() > 0, "'key' should not be empty.");
            String path = category + "." + key;
            return SOUNDS.register(path, () -> new SoundEvent(Exposure.resource(path), 16f));
        }
    }

    public static class Tags {
        public static class Items {
            public static final TagKey<Item> FILM_ROLLS = ItemTags.create(Exposure.resource("film_rolls"));
            public static final TagKey<Item> DEVELOPED_FILM_ROLLS = ItemTags.create(Exposure.resource("developed_film_rolls"));
            public static final TagKey<Item> CYAN_PRINTING_DYES = ItemTags.create(Exposure.resource("cyan_printing_dyes"));
            public static final TagKey<Item> MAGENTA_PRINTING_DYES = ItemTags.create(Exposure.resource("magenta_printing_dyes"));
            public static final TagKey<Item> YELLOW_PRINTING_DYES = ItemTags.create(Exposure.resource("yellow_printing_dyes"));
            public static final TagKey<Item> BLACK_PRINTING_DYES = ItemTags.create(Exposure.resource("black_printing_dyes"));
            public static final TagKey<Item> PHOTO_PAPERS = ItemTags.create(Exposure.resource("photo_papers"));
            public static final TagKey<Item> FILTERS = ItemTags.create(Exposure.resource("filters"));
        }
    }
}
