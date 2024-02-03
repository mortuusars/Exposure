package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.advancement.trigger.CameraFilmFrameExposedTrigger;
import io.github.mortuusars.exposure.block.FlashBlock;
import io.github.mortuusars.exposure.block.LightroomBlock;
import io.github.mortuusars.exposure.block.entity.FlashBlockEntity;
import io.github.mortuusars.exposure.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.camera.infrastructure.FilmType;
import io.github.mortuusars.exposure.entity.PhotographEntity;
import io.github.mortuusars.exposure.item.*;
import io.github.mortuusars.exposure.menu.AlbumMenu;
import io.github.mortuusars.exposure.menu.CameraAttachmentsMenu;
import io.github.mortuusars.exposure.menu.LecternAlbumMenu;
import io.github.mortuusars.exposure.menu.LightroomMenu;
import io.github.mortuusars.exposure.recipe.FilmDevelopingRecipe;
import io.github.mortuusars.exposure.recipe.PhotographCopyingRecipe;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class Exposure {
    public static final String ID = "exposure";

    public static final int DEFAULT_FILM_SIZE = 320;
    public static final float CROP_FACTOR = 1.142857f;

    public static void init() {
        Blocks.init();
        BlockEntityTypes.init();
        Items.init();
        EntityTypes.init();
        MenuTypes.init();
        RecipeSerializers.init();
        SoundEvents.init();
    }

    public static void initServer(MinecraftServer server) {
        ExposureServer.init(server);
    }

    /**
     * Creates resource location in the mod namespace with the given path.
     */
    public static ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
    }

    public static class Blocks {
        public static final Supplier<LightroomBlock> LIGHTROOM = Register.block("lightroom",
                () -> new LightroomBlock(BlockBehaviour.Properties.of(Material.WOOD)
                        .color(MaterialColor.COLOR_BROWN)
                        .strength(2.5f)
                        .sound(SoundType.WOOD)
                        .lightLevel(state -> 15)));

        public static final Supplier<FlashBlock> FLASH = Register.block("flash",
                () -> new FlashBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.AIR)
                        .strength(-1.0F, 3600000.8F)
                        .noLootTable()
                        .color(MaterialColor.NONE)
                        .noOcclusion()
                        .noCollission()
                        .lightLevel(state -> 15)));

        static void init() {
        }
    }

    public static class BlockEntityTypes {
        public static final Supplier<BlockEntityType<LightroomBlockEntity>> LIGHTROOM =
                Register.blockEntityType("lightroom", () -> Register.newBlockEntityType(LightroomBlockEntity::new, Blocks.LIGHTROOM.get()));

        public static final Supplier<BlockEntityType<FlashBlockEntity>> FLASH =
                Register.blockEntityType("flash", () -> Register.newBlockEntityType(FlashBlockEntity::new, Blocks.FLASH.get()));

        static void init() {
        }
    }

    public static class Items {
        public static final Supplier<CameraItem> CAMERA = Register.item("camera",
                () -> new CameraItem(new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final Supplier<FilmRollItem> BLACK_AND_WHITE_FILM = Register.item("black_and_white_film",
                () -> new FilmRollItem(FilmType.BLACK_AND_WHITE, Exposure.DEFAULT_FILM_SIZE, Mth.color(0.8F, 0.8F, 0.9F),
                        new Item.Properties()
                                .stacksTo(16)
                                .tab(CreativeModeTab.TAB_TOOLS)));

        public static final Supplier<FilmRollItem> COLOR_FILM = Register.item("color_film",
                () -> new FilmRollItem(FilmType.COLOR, Exposure.DEFAULT_FILM_SIZE, Mth.color(0.4F, 0.4F, 1.0F), new Item.Properties()
                        .stacksTo(16)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final Supplier<DevelopedFilmItem> DEVELOPED_BLACK_AND_WHITE_FILM = Register.item("developed_black_and_white_film",
                () -> new DevelopedFilmItem(FilmType.BLACK_AND_WHITE, new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final Supplier<DevelopedFilmItem> DEVELOPED_COLOR_FILM = Register.item("developed_color_film",
                () -> new DevelopedFilmItem(FilmType.COLOR, new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final Supplier<PhotographItem> PHOTOGRAPH = Register.item("photograph",
                () -> new PhotographItem(new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final Supplier<StackedPhotographsItem> STACKED_PHOTOGRAPHS = Register.item("stacked_photographs",
                () -> new StackedPhotographsItem(16, new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));

        public static final Supplier<AlbumItem> ALBUM = Register.item("album",
                () -> new AlbumItem(new Item.Properties()
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_TOOLS)));
        public static final Supplier<SignedAlbumItem> SIGNED_ALBUM = Register.item("signed_album",
                () -> new SignedAlbumItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<BlockItem> LIGHTROOM = Register.item("lightroom",
                () -> new BlockItem(Blocks.LIGHTROOM.get(), new Item.Properties()
                        .tab(CreativeModeTab.TAB_DECORATIONS)));

        static void init() {
        }
    }

    public static class EntityTypes {
        public static final Supplier<EntityType<PhotographEntity>> PHOTOGRAPH = Register.entityType("photograph",
                PhotographEntity::new, MobCategory.MISC, 0.5F, 0.5F, 128, false, Integer.MAX_VALUE);

        static void init() {
        }
    }

    public static class MenuTypes {
        public static final Supplier<MenuType<CameraAttachmentsMenu>> CAMERA = Register.menuType("camera", CameraAttachmentsMenu::fromBuffer);
        public static final Supplier<MenuType<AlbumMenu>> ALBUM = Register.menuType("album", AlbumMenu::fromBuffer);
        public static final Supplier<MenuType<LecternAlbumMenu>> LECTERN_ALBUM = Register.menuType("lectern_album", LecternAlbumMenu::fromBuffer);
        public static final Supplier<MenuType<LightroomMenu>> LIGHTROOM = Register.menuType("lightroom", LightroomMenu::fromBuffer);

        static void init() {
        }
    }

    public static class RecipeSerializers {
        public static final Supplier<RecipeSerializer<?>> FILM_DEVELOPING = Register.recipeSerializer("film_developing",
                FilmDevelopingRecipe.Serializer::new);
        public static final Supplier<RecipeSerializer<?>> PHOTOGRAPH_CLONING = Register.recipeSerializer("photograph_copying",
                PhotographCopyingRecipe.Serializer::new);

        static void init() {
        }
    }

    public static class SoundEvents {
        public static final Supplier<SoundEvent> VIEWFINDER_OPEN = register("item", "camera.viewfinder_open");
        public static final Supplier<SoundEvent> VIEWFINDER_CLOSE = register("item", "camera.viewfinder_close");
        public static final Supplier<SoundEvent> SHUTTER_OPEN = register("item", "camera.shutter_open");
        public static final Supplier<SoundEvent> SHUTTER_CLOSE = register("item", "camera.shutter_close");
        public static final Supplier<SoundEvent> SHUTTER_TICKING = register("item", "camera.shutter_ticking");
        public static final Supplier<SoundEvent> FILM_ADVANCE = register("item", "camera.film_advance");
        public static final Supplier<SoundEvent> FILM_ADVANCE_LAST = register("item", "camera.film_advance_last");
        public static final Supplier<SoundEvent> CAMERA_BUTTON_CLICK = register("item", "camera.button_click");
        public static final Supplier<SoundEvent> CAMERA_RELEASE_BUTTON_CLICK = register("item", "camera.release_button_click");
        public static final Supplier<SoundEvent> CAMERA_DIAL_CLICK = register("item", "camera.dial_click");
        public static final Supplier<SoundEvent> CAMERA_LENS_RING_CLICK = register("item", "camera.lens_ring_click");
        public static final Supplier<SoundEvent> FILTER_PLACE = register("item", "camera.filter_place");
        public static final Supplier<SoundEvent> FLASH = register("item", "camera.flash");

        public static final Supplier<SoundEvent> PHOTOGRAPH_PLACE = register("item", "photograph.place");
        public static final Supplier<SoundEvent> PHOTOGRAPH_BREAK = register("item", "photograph.break");
        public static final Supplier<SoundEvent> PHOTOGRAPH_RUSTLE = register("item", "photograph.rustle");

        public static final Supplier<SoundEvent> LIGHTROOM_PRINT = register("block", "lightroom.print");

        private static Supplier<SoundEvent> register(String category, String key) {
            Preconditions.checkState(category != null && !category.isEmpty(), "'category' should not be empty.");
            Preconditions.checkState(key != null && !key.isEmpty(), "'key' should not be empty.");
            String path = category + "." + key;
            return Register.soundEvent(path, () -> new SoundEvent(Exposure.resource(path), 16f));
        }

        static void init() {
        }
    }

    public static class Stats {
        private static final Map<ResourceLocation, StatFormatter> STATS = new HashMap<>();

        public static final ResourceLocation INTERACT_WITH_LIGHTROOM =
                register(Exposure.resource("interact_with_lightroom"), StatFormatter.DEFAULT);
        public static final ResourceLocation FILM_FRAMES_EXPOSED =
                register(Exposure.resource("film_frames_exposed"), StatFormatter.DEFAULT);
        public static final ResourceLocation FLASHES_TRIGGERED =
                register(Exposure.resource("flashes_triggered"), StatFormatter.DEFAULT);

        @SuppressWarnings("SameParameterValue")
        private static ResourceLocation register(ResourceLocation location, StatFormatter formatter) {
            STATS.put(location, formatter);
            return location;
        }

        public static void register() {
            STATS.forEach((location, formatter) -> {
                Registry.register(Registry.CUSTOM_STAT, location, location);
                net.minecraft.stats.Stats.CUSTOM.get(location, formatter);
            });
        }
    }

    public static class Advancements {
        public static CameraFilmFrameExposedTrigger FILM_FRAME_EXPOSED = new CameraFilmFrameExposedTrigger();

        public static void register() {
            CriteriaTriggers.register(FILM_FRAME_EXPOSED);
        }
    }

    public static class Tags {
        public static class Items {
            public static final TagKey<Item> FILM_ROLLS = TagKey.create(Registry.ITEM_REGISTRY, Exposure.resource("film_rolls"));
            public static final TagKey<Item> DEVELOPED_FILM_ROLLS = TagKey.create(Registry.ITEM_REGISTRY, Exposure.resource("developed_film_rolls"));
            public static final TagKey<Item> CYAN_PRINTING_DYES = TagKey.create(Registry.ITEM_REGISTRY, Exposure.resource("cyan_printing_dyes"));
            public static final TagKey<Item> MAGENTA_PRINTING_DYES = TagKey.create(Registry.ITEM_REGISTRY, Exposure.resource("magenta_printing_dyes"));
            public static final TagKey<Item> YELLOW_PRINTING_DYES = TagKey.create(Registry.ITEM_REGISTRY, Exposure.resource("yellow_printing_dyes"));
            public static final TagKey<Item> BLACK_PRINTING_DYES = TagKey.create(Registry.ITEM_REGISTRY, Exposure.resource("black_printing_dyes"));
            public static final TagKey<Item> PHOTO_PAPERS = TagKey.create(Registry.ITEM_REGISTRY, Exposure.resource("photo_papers"));
            public static final TagKey<Item> FLASHES = TagKey.create(Registry.ITEM_REGISTRY, Exposure.resource("flashes"));
            public static final TagKey<Item> LENSES = TagKey.create(Registry.ITEM_REGISTRY, Exposure.resource("lenses"));
            public static final TagKey<Item> FILTERS = TagKey.create(Registry.ITEM_REGISTRY, Exposure.resource("filters"));
        }
    }
}
