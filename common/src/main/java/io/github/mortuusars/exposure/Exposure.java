package io.github.mortuusars.exposure;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.advancements.predicate.FramePredicate;
import io.github.mortuusars.exposure.advancements.predicate.TamedPredicate;
import io.github.mortuusars.exposure.advancements.trigger.FrameExposedTrigger;
import io.github.mortuusars.exposure.advancements.trigger.FramePrintedTrigger;
import io.github.mortuusars.exposure.network.packet.clientbound.*;
import io.github.mortuusars.exposure.network.packet.common.CommonActiveCameraDeactivatePacket;
import io.github.mortuusars.exposure.network.packet.serverbound.*;
import io.github.mortuusars.exposure.world.block.FlashBlock;
import io.github.mortuusars.exposure.world.block.LightroomBlock;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.commands.argument.*;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmStyle;
import io.github.mortuusars.exposure.world.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.world.camera.component.FlashMode;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.DitherMode;
import io.github.mortuusars.exposure.world.camera.component.SelfTimer;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Lens;
import io.github.mortuusars.exposure.world.entity.stand.CameraStand;
import io.github.mortuusars.exposure.world.entity.GlassPhotographFrame;
import io.github.mortuusars.exposure.world.entity.PhotographFrame;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.stand.EquipmentStand;
import io.github.mortuusars.exposure.world.inventory.*;
import io.github.mortuusars.exposure.world.item.*;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.item.component.StackedPhotographs;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.item.component.album.AlbumContent;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumContent;
import io.github.mortuusars.exposure.world.item.camera.ShutterState;
import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.FilmDevelopingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.PhotographAgingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.PhotographCopyingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.serializer.ComponentTransferringRecipeSerializer;
import io.github.mortuusars.mortaar.Register;
import io.github.mortuusars.mortaar.Registrar;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Supplier;


public class Exposure {
    public static final String ID = "exposure";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Registrar REGISTRAR = Register.registrar(ID);

    public static final int MAX_ENTITIES_IN_FRAME = 10;

    public static void init() {
        Blocks.init();
        BlockEntityTypes.init();
        EntityTypes.init();
        Items.init();
        CreativeTabs.init();
        DataComponents.init();
        CriteriaTriggers.init();
        ItemSubPredicates.init();
        EntitySubPredicates.init();
        MenuTypes.init();
        RecipeSerializers.init();
        SoundEvents.init();
        ArgumentTypes.init();
        Stats.init();

        Register.serverboundPacket(ServerboundAlbumSignPacket.TYPE, ServerboundAlbumSignPacket.STREAM_CODEC);
        Register.serverboundPacket(ServerboundAlbumSyncNotePacket.TYPE, ServerboundAlbumSyncNotePacket.STREAM_CODEC);
        Register.serverboundPacket(ServerboundActiveCameraSetSettingPacket.TYPE, ServerboundActiveCameraSetSettingPacket.STREAM_CODEC);
        Register.serverboundPacket(ServerboundOpenCameraAttachmentsInCreativePacket.TYPE, ServerboundOpenCameraAttachmentsInCreativePacket.STREAM_CODEC);
        Register.serverboundPacket(ServerboundExposureRequestPacket.TYPE, ServerboundExposureRequestPacket.STREAM_CODEC);
        Register.serverboundPacket(ServerboundActiveCameraReleasePacket.TYPE, ServerboundActiveCameraReleasePacket.STREAM_CODEC);
        Register.serverboundPacket(ServerboundInterplanarProjectionFinishedPacket.TYPE, ServerboundInterplanarProjectionFinishedPacket.STREAM_CODEC);
        Register.serverboundPacket(ServerboundExposureDataPacket.TYPE, ServerboundExposureDataPacket.STREAM_CODEC);
        Register.serverboundPacket(ServerboundCameraStandTurnPacket.TYPE, ServerboundCameraStandTurnPacket.STREAM_CODEC);

        Register.clientboundPacket(ClientboundActiveCameraRemovePacket.TYPE, ClientboundActiveCameraRemovePacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundActiveCameraInHandSetPacket.TYPE, ClientboundActiveCameraInHandSetPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundActiveCameraOnStandSetPacket.TYPE, ClientboundActiveCameraOnStandSetPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundCameraStandSetRotationsPacket.TYPE, ClientboundCameraStandSetRotationsPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundCameraStandStopControllingPacket.TYPE, ClientboundCameraStandStopControllingPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundShaderApplyPacket.TYPE, ClientboundShaderApplyPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundClearRenderingCachePacket.TYPE, ClientboundClearRenderingCachePacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundCreateChromaticExposurePacket.TYPE, ClientboundCreateChromaticExposurePacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundExposureDataChangedPacket.TYPE, ClientboundExposureDataChangedPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundUniqueSoundPlayPacket.TYPE, ClientboundUniqueSoundPlayPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundUniqueSoundPlayShutterTickingPacket.TYPE, ClientboundUniqueSoundPlayShutterTickingPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundUniqueSoundStopPacket.TYPE, ClientboundUniqueSoundStopPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundShowExposureCommandPacket.TYPE, ClientboundShowExposureCommandPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundExposureDataResponsePacket.TYPE, ClientboundExposureDataResponsePacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundShutterOpenedPacket.TYPE, ClientboundShutterOpenedPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundCaptureStartPacket.TYPE, ClientboundCaptureStartPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundCaptureStartDebugRGBPacket.TYPE, ClientboundCaptureStartDebugRGBPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundExportPacket.TYPE, ClientboundExportPacket.STREAM_CODEC);
        Register.clientboundPacket(ClientboundExportStopPacket.TYPE, ClientboundExportStopPacket.STREAM_CODEC);

        Register.bidirectionalPacket(CommonActiveCameraDeactivatePacket.TYPE, CommonActiveCameraDeactivatePacket.STREAM_CODEC);
    }

    public static void initServer(MinecraftServer server) {
        ExposureServer.init(server);
    }

    /**
     * Creates resource location in the mod namespace with the given filePath.
     */
    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static class Blocks {
        public static final Supplier<LightroomBlock> LIGHTROOM = REGISTRAR.block("lightroom",
                () -> new LightroomBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BROWN)
                        .strength(2.5f)
                        .sound(SoundType.WOOD)));

        public static final Supplier<FlashBlock> FLASH = REGISTRAR.block("flash",
                () -> new FlashBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.AIR)
                        .strength(-1.0F, 3600000.8F)
                        .noLootTable()
                        .mapColor(MapColor.NONE)
                        .noOcclusion()
                        .noCollission()
                        .lightLevel(state -> 15)));

        static void init() {
        }
    }

    public static class BlockEntityTypes {
        public static final Supplier<BlockEntityType<LightroomBlockEntity>> LIGHTROOM =
                REGISTRAR.blockEntityType("lightroom", () -> REGISTRAR.newBlockEntityType(LightroomBlockEntity::new, Blocks.LIGHTROOM.get()));

        static void init() {
        }
    }

    public static class Items {
        public static final Supplier<CameraItem> CAMERA = REGISTRAR.item("camera",
                () -> new CameraItem(new Item.Properties()
                        .stacksTo(1)
                        .component(DataComponents.CAMERA_ACTIVE, false)));

        public static final Supplier<FilmRollItem> BLACK_AND_WHITE_FILM = REGISTRAR.item("black_and_white_film",
                () -> new FilmRollItem(ExposureType.BLACK_AND_WHITE, FilmRollItem.BAR_BLACK_AND_WHITE,
                        new Item.Properties()
                                .stacksTo(16)));

        public static final Supplier<FilmRollItem> COLOR_FILM = REGISTRAR.item("color_film",
                () -> new FilmRollItem(ExposureType.COLOR, FilmRollItem.BAR_COLOR,
                        new Item.Properties()
                                .stacksTo(16)));

        public static final Supplier<FilmRollItem> HIGH_SENSITIVITY_BLACK_AND_WHITE_FILM = REGISTRAR.item("high_sensitivity_black_and_white_film",
                () -> new FilmRollItem(ExposureType.BLACK_AND_WHITE, FilmRollItem.BAR_BLACK_AND_WHITE,
                        new Item.Properties()
                                .component(DataComponents.FILM_STYLE, FilmStyle.create()
                                        .withSensitivity(2f)
                                        .withNoise(0.065f))
                                .stacksTo(16)));

        public static final Supplier<FilmRollItem> HIGH_SENSITIVITY_COLOR_FILM = REGISTRAR.item("high_sensitivity_color_film",
                () -> new FilmRollItem(ExposureType.COLOR, FilmRollItem.BAR_COLOR,
                        new Item.Properties()
                                .component(DataComponents.FILM_STYLE, FilmStyle.create()
                                        .withSensitivity(2f)
                                        .withNoise(0.065f))
                                .stacksTo(16)));

        public static final Supplier<DevelopedFilmItem> DEVELOPED_BLACK_AND_WHITE_FILM = REGISTRAR.item("developed_black_and_white_film",
                () -> new DevelopedFilmItem(ExposureType.BLACK_AND_WHITE, new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<DevelopedFilmItem> DEVELOPED_COLOR_FILM = REGISTRAR.item("developed_color_film",
                () -> new DevelopedFilmItem(ExposureType.COLOR, new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<PhotographItem> PHOTOGRAPH = REGISTRAR.item("photograph",
                () -> new PhotographItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<ChromaticSheetItem> CHROMATIC_SHEET = REGISTRAR.item("chromatic_sheet",
                () -> new ChromaticSheetItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<PhotographItem> AGED_PHOTOGRAPH = REGISTRAR.item("aged_photograph",
                () -> new AgedPhotographItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<InterplanarProjectorItem> INTERPLANAR_PROJECTOR = REGISTRAR.item("interplanar_projector",
                () -> new InterplanarProjectorItem(new Item.Properties()));
        public static final Supplier<BrokenInterplanarProjectorItem> BROKEN_INTERPLANAR_PROJECTOR = REGISTRAR.item("broken_interplanar_projector",
                () -> new BrokenInterplanarProjectorItem(new Item.Properties()));

        public static final Supplier<StackedPhotographsItem> STACKED_PHOTOGRAPHS = REGISTRAR.item("stacked_photographs",
                () -> new StackedPhotographsItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<AlbumItem> ALBUM = REGISTRAR.item("album",
                () -> new AlbumItem(new Item.Properties()
                        .stacksTo(1)));
        public static final Supplier<SignedAlbumItem> SIGNED_ALBUM = REGISTRAR.item("signed_album",
                () -> new SignedAlbumItem(new Item.Properties()
                        .stacksTo(1)));

        public static final Supplier<PhotographFrameItem> PHOTOGRAPH_FRAME = REGISTRAR.item("photograph_frame",
                () -> new PhotographFrameItem(new Item.Properties()));
        public static final Supplier<GlassPhotographFrameItem> CLEAR_PHOTOGRAPH_FRAME = REGISTRAR.item("glass_photograph_frame",
                () -> new GlassPhotographFrameItem(new Item.Properties()));

        public static final Supplier<EquipmentStandItem> EQUIPMENT_STAND = REGISTRAR.item("equipment_stand",
                () -> new EquipmentStandItem(new Item.Properties()));

        public static final Supplier<BlockItem> LIGHTROOM = REGISTRAR.item("lightroom",
                () -> new BlockItem(Blocks.LIGHTROOM.get(), new Item.Properties()));

        static void init() {
        }
    }

    public static class CreativeTabs {
        public static final Supplier<CreativeModeTab> EXPOSURE = REGISTRAR.creativeTab("exposure", () ->
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                        .title(Component.translatable("itemGroup.exposure.exposure"))
                        .icon(() -> new ItemStack(Items.CAMERA.get()))
                        .displayItems((params, output) -> {
                            output.accept(Items.CAMERA.get());
                            output.accept(Items.EQUIPMENT_STAND.get());
                            output.accept(Items.INTERPLANAR_PROJECTOR.get());
                            output.accept(Items.BLACK_AND_WHITE_FILM.get());
                            output.accept(Items.COLOR_FILM.get());
                            output.accept(Items.HIGH_SENSITIVITY_BLACK_AND_WHITE_FILM.get());
                            output.accept(Items.HIGH_SENSITIVITY_COLOR_FILM.get());
                            output.accept(Items.DEVELOPED_BLACK_AND_WHITE_FILM.get());
                            output.accept(Items.DEVELOPED_COLOR_FILM.get());
                            output.accept(Items.PHOTOGRAPH.get());
                            output.accept(Items.AGED_PHOTOGRAPH.get());
                            output.accept(Items.STACKED_PHOTOGRAPHS.get());
                            output.accept(Items.ALBUM.get());
                            output.accept(Items.PHOTOGRAPH_FRAME.get());
                            output.accept(Items.CLEAR_PHOTOGRAPH_FRAME.get());
                            output.accept(Items.LIGHTROOM.get());
                        })
                        .build());

        static void init() {
        }
    }

    public static class DataComponents {
        // Camera State

        public static final DataComponentType<CameraId> CAMERA_ID = REGISTRAR.dataComponentType("camera_id",
                arg -> arg.persistent(CameraId.CODEC).networkSynchronized(CameraId.STREAM_CODEC));

        public static final DataComponentType<Boolean> CAMERA_GOLD = REGISTRAR.dataComponentType("camera_gold",
                arg -> arg.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

        public static final DataComponentType<Boolean> CAMERA_ACTIVE = REGISTRAR.dataComponentType("camera_active",
                arg -> arg.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

        public static final DataComponentType<Boolean> CAMERA_DISASSEMBLED = REGISTRAR.dataComponentType("camera_disassembled",
                arg -> arg.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

        public static final DataComponentType<Long> CAMERA_LAST_ACTION_TIME = REGISTRAR.dataComponentType("camera_last_action_time",
                arg -> arg.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

        public static final DataComponentType<Boolean> SELFIE_MODE = REGISTRAR.dataComponentType("camera_selfie_mode",
                arg -> arg.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

        public static final DataComponentType<ShutterState> SHUTTER_STATE = REGISTRAR.dataComponentType("camera_shutter_state",
                arg -> arg.persistent(ShutterState.CODEC).networkSynchronized(ShutterState.STREAM_CODEC));

        public static final DataComponentType<Long> TIMER_START_TICK = REGISTRAR.dataComponentType("camera_timer_start_tick",
                arg -> arg.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

        public static final DataComponentType<Long> TIMER_END_TICK = REGISTRAR.dataComponentType("camera_timer_end_tick",
                arg -> arg.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

        public static final DataComponentType<Long> TIMER_LAST_RELEASE_TICK = REGISTRAR.dataComponentType("camera_timer_last_release_tick",
                arg -> arg.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

        // Settings

        public static final DataComponentType<ShutterSpeed> SHUTTER_SPEED = REGISTRAR.dataComponentType("camera_shutter_speed",
                arg -> arg.persistent(ShutterSpeed.CODEC).networkSynchronized(ShutterSpeed.STREAM_CODEC));

        public static final DataComponentType<CompositionGuide> COMPOSITION_GUIDE = REGISTRAR.dataComponentType("camera_composition_guide",
                arg -> arg.persistent(CompositionGuide.CODEC).networkSynchronized(CompositionGuide.STREAM_CODEC));

        public static final DataComponentType<SelfTimer> SELF_TIMER = REGISTRAR.dataComponentType("camera_self_timer",
                arg -> arg.persistent(SelfTimer.CODEC).networkSynchronized(SelfTimer.STREAM_CODEC));

        public static final DataComponentType<Float> ZOOM = REGISTRAR.dataComponentType("camera_zoom",
                arg -> arg.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));

        public static final DataComponentType<Double> SELFIE_ROTATION_X = REGISTRAR.dataComponentType("camera_selfie_rotation_x",
                arg -> arg.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

        public static final DataComponentType<Double> SELFIE_ROTATION_Y = REGISTRAR.dataComponentType("camera_selfie_rotation_y",
                arg -> arg.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

        public static final DataComponentType<FlashMode> FLASH_MODE = REGISTRAR.dataComponentType("camera_flash_mode",
                arg -> arg.persistent(FlashMode.CODEC).networkSynchronized(FlashMode.STREAM_CODEC));

        // Attachments

        public static final DataComponentType<StoredItemStack> FILM = REGISTRAR.dataComponentType("camera_film",
                arg -> arg.persistent(StoredItemStack.CODEC).networkSynchronized(StoredItemStack.STREAM_CODEC));

        public static final DataComponentType<StoredItemStack> FLASH = REGISTRAR.dataComponentType("camera_flash",
                arg -> arg.persistent(StoredItemStack.CODEC).networkSynchronized(StoredItemStack.STREAM_CODEC));

        public static final DataComponentType<StoredItemStack> LENS = REGISTRAR.dataComponentType("camera_lens",
                arg -> arg.persistent(StoredItemStack.CODEC).networkSynchronized(StoredItemStack.STREAM_CODEC));

        public static final DataComponentType<StoredItemStack> FILTER = REGISTRAR.dataComponentType("camera_filter",
                arg -> arg.persistent(StoredItemStack.CODEC).networkSynchronized(StoredItemStack.STREAM_CODEC));

        // Film

        public static final DataComponentType<Integer> FILM_FRAME_COUNT = REGISTRAR.dataComponentType("film_frame_count",
                arg -> arg.persistent(ExtraCodecs.intRange(1, 256)).networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DataComponentType<Integer> FILM_FRAME_SIZE = REGISTRAR.dataComponentType("film_frame_size",
                arg -> arg.persistent(ExtraCodecs.intRange(1, 2048)).networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DataComponentType<FilmStyle> FILM_STYLE = REGISTRAR.dataComponentType("film_style",
                arg -> arg.persistent(FilmStyle.CODEC).networkSynchronized(FilmStyle.STREAM_CODEC));

        public static final DataComponentType<ResourceLocation> FILM_COLOR_PALETTE = REGISTRAR.dataComponentType("film_color_palette",
                arg -> arg.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC));

        public static final DataComponentType<DitherMode> FILM_DITHER_MODE = REGISTRAR.dataComponentType("film_dither_mode",
                arg -> arg.persistent(DitherMode.CODEC).networkSynchronized(DitherMode.STREAM_CODEC));

        public static final DataComponentType<List<Frame>> FILM_FRAMES =
                REGISTRAR.dataComponentType("film_frames",
                        arg -> arg.persistent(Frame.CODEC.listOf(0, 256))
                                .networkSynchronized(Frame.STREAM_CODEC.apply(ByteBufCodecs.list(256))));

        // Photograph

        public static final DataComponentType<Frame> PHOTOGRAPH_FRAME = REGISTRAR.dataComponentType("photograph_frame",
                arg -> arg.persistent(Frame.CODEC).networkSynchronized(Frame.STREAM_CODEC));

        public static final DataComponentType<ExposureType> PHOTOGRAPH_TYPE = REGISTRAR.dataComponentType("photograph_type",
                arg -> arg.persistent(ExposureType.CODEC).networkSynchronized(ExposureType.STREAM_CODEC));

        public static final DataComponentType<Integer> PHOTOGRAPH_GENERATION = REGISTRAR.dataComponentType("photograph_generation",
                arg -> arg.persistent(ExtraCodecs.intRange(0, 3)).networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DataComponentType<StackedPhotographs> STACKED_PHOTOGRAPHS =
                REGISTRAR.dataComponentType("stacked_photographs",
                      arg -> arg.persistent(StackedPhotographs.CODEC).networkSynchronized(StackedPhotographs.STREAM_CODEC).cacheEncoding());

        // Album

        public static final DataComponentType<AlbumContent> ALBUM_CONTENT = REGISTRAR.dataComponentType("album_content",
                arg -> arg.persistent(AlbumContent.CODEC).networkSynchronized(AlbumContent.STREAM_CODEC));

        public static final DataComponentType<SignedAlbumContent> SIGNED_ALBUM_CONTENT = REGISTRAR.dataComponentType("signed_album_content",
                arg -> arg.persistent(SignedAlbumContent.CODEC).networkSynchronized(SignedAlbumContent.STREAM_CODEC));

        // --

        public static final DataComponentType<DitherMode> INTERPLANAR_PROJECTOR_MODE =
                REGISTRAR.dataComponentType("interplanar_projector_mode",
                        arg -> arg.persistent(DitherMode.CODEC)
                                .networkSynchronized(DitherMode.STREAM_CODEC));

        public static final DataComponentType<String> INTERPLANAR_PROJECTOR_ERROR_CODE =
                REGISTRAR.dataComponentType("interplanar_projector_error_code",
                        arg -> arg.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

        public static final DataComponentType<List<Frame>> CHROMATIC_SHEET_LAYERS =
                REGISTRAR.dataComponentType("chromatic_layers",
                        arg -> arg.persistent(Frame.CODEC.listOf(0, 3))
                                .networkSynchronized(Frame.STREAM_CODEC.apply(ByteBufCodecs.list())));

        static void init() {
        }
    }

    public static class EntityTypes {
        public static final Supplier<EntityType<PhotographFrame>> PHOTOGRAPH_FRAME = REGISTRAR.entityType("photograph_frame",
                PhotographFrame::new, MobCategory.MISC, false, builder -> builder
                        .sized(0.5f, 0.5f)
                        .updateInterval(Integer.MAX_VALUE)
                        .eyeHeight(0));

        public static final Supplier<EntityType<GlassPhotographFrame>> CLEAR_PHOTOGRAPH_FRAME = REGISTRAR.entityType("glass_photograph_frame",
                GlassPhotographFrame::new, MobCategory.MISC, false, builder -> builder
                        .sized(0.5f, 0.5f)
                        .updateInterval(Integer.MAX_VALUE)
                        .eyeHeight(0));

        public static final Supplier<EntityType<EquipmentStand>> EQUIPMENT_STAND = REGISTRAR.entityType("equipment_stand",
              EquipmentStand::new, MobCategory.MISC, false, builder -> builder
                    .sized(0.7f, 1.6f)
                    .updateInterval(3)
                    .eyeHeight(1.40625f));

        public static final Supplier<EntityType<CameraStand>> CAMERA_STAND = REGISTRAR.entityType("camera_stand",
                CameraStand::new, MobCategory.MISC, false, builder -> builder
                        .sized(0.7f, 1.6f)
                        .updateInterval(3)
                        .eyeHeight(1.40625f));

        static void init() {
        }
    }

    public static class MenuTypes {
        public static final Supplier<MenuType<CameraInHandAttachmentsMenu>> CAMERA_IN_HAND = REGISTRAR.menuType("camera_in_hand", CameraInHandAttachmentsMenu::fromBuffer);
        public static final Supplier<MenuType<CameraOnStandAttachmentsMenu>> CAMERA_ON_STAND = REGISTRAR.menuType("camera_on_stand", CameraOnStandAttachmentsMenu::fromNetwork);
        public static final Supplier<MenuType<AlbumMenu>> ALBUM = REGISTRAR.menuType("album", AlbumMenu::fromBuffer);
        public static final Supplier<MenuType<SignedAlbumMenu>> SIGNED_ALBUM = REGISTRAR.menuType("signed_album", SignedAlbumMenu::fromBuffer);
        public static final Supplier<MenuType<LecternAlbumMenu>> LECTERN_ALBUM = REGISTRAR.menuType("lectern_album", LecternAlbumMenu::new);
        public static final Supplier<MenuType<LightroomMenu>> LIGHTROOM = REGISTRAR.menuType("lightroom", LightroomMenu::fromBuffer);
        public static final Supplier<MenuType<ItemRenameMenu>> ITEM_RENAME = REGISTRAR.menuType("item_rename", ItemRenameMenu::fromBuffer);

        static void init() {
        }
    }

    public static class RecipeSerializers {
        public static final Supplier<RecipeSerializer<FilmDevelopingRecipe>> FILM_DEVELOPING =
                registerTransferring("film_developing", "film", FilmDevelopingRecipe::new);
        public static final Supplier<RecipeSerializer<PhotographCopyingRecipe>> PHOTOGRAPH_COPYING =
                registerTransferring("photograph_copying", "photograph", PhotographCopyingRecipe::new);
        public static final Supplier<RecipeSerializer<PhotographAgingRecipe>> PHOTOGRAPH_AGING =
                registerTransferring("photograph_aging", "photograph", PhotographAgingRecipe::new);
        public static final Supplier<RecipeSerializer<ComponentTransferringRecipe>> COMPONENT_TRANSFERRING =
                registerTransferring("component_transferring", "source", ComponentTransferringRecipe::new);

        private static <T extends ComponentTransferringRecipe> Supplier<RecipeSerializer<T>> registerTransferring(
                String name, String sourceName, ComponentTransferringRecipeSerializer.RecipeConstructor<T> recipeConstructor) {
            return REGISTRAR.recipeSerializer(name, () -> new ComponentTransferringRecipeSerializer<>(name, sourceName, recipeConstructor));
        }

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
        public static final Supplier<SoundEvent> FILM_REMOVED = register("item", "camera.film_removed");
        public static final Supplier<SoundEvent> CAMERA_GENERIC_CLICK = register("item", "camera.generic_click");
        public static final Supplier<SoundEvent> CAMERA_BUTTON_CLICK = register("item", "camera.button_click");
        public static final Supplier<SoundEvent> CAMERA_RELEASE_BUTTON_CLICK = register("item", "camera.release_button_click");
        public static final Supplier<SoundEvent> CAMERA_DIAL_CLICK = register("item", "camera.dial_click");
        public static final Supplier<SoundEvent> CAMERA_LENS_RING_CLICK = register("item", "camera.lens_ring_click");
        public static final Supplier<SoundEvent> CAMERA_TIMER_TICK = register("item", "camera.timer_tick");
        public static final Supplier<SoundEvent> LENS_INSERT = register("item", "camera.lens_insert");
        public static final Supplier<SoundEvent> LENS_REMOVE = register("item", "camera.lens_remove");
        public static final Supplier<SoundEvent> FILTER_INSERT = register("item", "camera.filter_insert");
        public static final Supplier<SoundEvent> FILTER_REMOVE = register("item", "camera.filter_remove");
        public static final Supplier<SoundEvent> FLASH = register("item", "camera.flash");
        public static final Supplier<SoundEvent> INTERPLANAR_PROJECT = register("item", "camera.interplanar_projector.project");

        public static final Supplier<SoundEvent> PHOTOGRAPH_PLACE = register("item", "photograph.place");
        public static final Supplier<SoundEvent> PHOTOGRAPH_BREAK = register("item", "photograph.break");
        public static final Supplier<SoundEvent> PHOTOGRAPH_RUSTLE = register("item", "photograph.rustle");

        public static final Supplier<SoundEvent> PHOTOGRAPH_FRAME_PLACE = register("item", "photograph_frame.place");
        public static final Supplier<SoundEvent> PHOTOGRAPH_FRAME_BREAK = register("item", "photograph_frame.break");
        public static final Supplier<SoundEvent> PHOTOGRAPH_FRAME_ADD_ITEM = register("item", "photograph_frame.add_item");
        public static final Supplier<SoundEvent> PHOTOGRAPH_FRAME_REMOVE_ITEM = register("item", "photograph_frame.remove_item");
        public static final Supplier<SoundEvent> PHOTOGRAPH_FRAME_ROTATE_ITEM = register("item", "photograph_frame.rotate_item");

        //TODO: Rename sounds (and subtitles)
        public static final Supplier<SoundEvent> CAMERA_STAND_PLACE = register("entity", "camera_stand.place");
        public static final Supplier<SoundEvent> CAMERA_STAND_HIT = register("entity", "camera_stand.hit");
        public static final Supplier<SoundEvent> CAMERA_STAND_BREAK = register("entity", "camera_stand.break");
        public static final Supplier<SoundEvent> CAMERA_STAND_SET_CAMERA = register("entity", "camera_stand.set_camera");
        public static final Supplier<SoundEvent> CAMERA_STAND_REMOVE_CAMERA = register("entity", "camera_stand.remove_camera");

        public static final Supplier<SoundEvent> LIGHTROOM_PRINT = register("block", "lightroom.print");

        public static final Supplier<SoundEvent> WRITE = register("misc", "write");
        public static final Supplier<SoundEvent> BSOD = register("misc", "bsod");

        private static Supplier<SoundEvent> register(String category, String key) {
            Preconditions.checkState(category != null && !category.isEmpty(), "'category' should not be empty.");
            Preconditions.checkState(key != null && !key.isEmpty(), "'key' should not be empty.");
            String path = category + "." + key;
            return REGISTRAR.soundEvent(path, () -> SoundEvent.createVariableRangeEvent(Exposure.resource(path)));
        }

        static void init() {
        }
    }

    public static class Stats {
        public static final Supplier<ResourceLocation> INTERACT_WITH_LIGHTROOM =
                REGISTRAR.stat(resource("interact_with_lightroom"), StatFormatter.DEFAULT);
        public static final Supplier<ResourceLocation> FILM_FRAMES_EXPOSED =
                REGISTRAR.stat(resource("film_frames_exposed"), StatFormatter.DEFAULT);
        public static final Supplier<ResourceLocation> FLASHES_TRIGGERED =
                REGISTRAR.stat(resource("flashes_triggered"), StatFormatter.DEFAULT);

        public static void init() {

        }
    }

    public static class CriteriaTriggers {
        public static Supplier<FrameExposedTrigger> FRAME_EXPOSED = REGISTRAR.criterionTrigger("frame_exposed", FrameExposedTrigger::new);
        public static Supplier<FramePrintedTrigger> FRAME_PRINTED = REGISTRAR.criterionTrigger("frame_printed", FramePrintedTrigger::new);
        public static Supplier<PlayerTrigger> PHOTOGRAPH_ENDERMAN_EYES = REGISTRAR.criterionTrigger("photograph_enderman_eyes", PlayerTrigger::new);
        public static Supplier<PlayerTrigger> SUCCESSFULLY_PROJECT_IMAGE = REGISTRAR.criterionTrigger("successfully_project_image", PlayerTrigger::new);

        public static void init() {
        }
    }

    public static class ItemSubPredicates {
        public static Supplier<ItemSubPredicate.Type<FramePredicate>> FRAME = REGISTRAR.itemSubPredicate("frame",
                () -> new ItemSubPredicate.Type<>(FramePredicate.CODEC));

        public static void init() {
        }
    }

    public static class EntitySubPredicates {
        public static final Supplier<MapCodec<TamedPredicate>> TAMED = REGISTRAR.entitySubPredicateType("tamed", () -> TamedPredicate.CODEC);

        public static void init() {
        }
    }

    public static class LootTables {
        public static final ResourceKey<LootTable> SIMPLE_DUNGEON_INJECT =
                ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Exposure.resource("chests/simple_dungeon"));
        public static final ResourceKey<LootTable> ABANDONED_MINESHAFT_INJECT =
                ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Exposure.resource("chests/abandoned_mineshaft"));
        public static final ResourceKey<LootTable> STRONGHOLD_CROSSING_INJECT =
                ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Exposure.resource("chests/stronghold_crossing"));
        public static final ResourceKey<LootTable> VILLAGE_PLAINS_HOUSE_INJECT =
                ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Exposure.resource("chests/village_plains_house"));
        public static final ResourceKey<LootTable> SHIPWRECK_MAP_INJECT =
                ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Exposure.resource("chests/shipwreck_map"));
    }

    public static class Tags {
        public static class Items {
            public static final TagKey<Item> FILM_ROLLS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("film_rolls"));
            public static final TagKey<Item> BLACK_AND_WHITE_FILM_ROLLS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("black_and_white_film_rolls"));
            public static final TagKey<Item> COLOR_FILM_ROLLS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("color_film_rolls"));
            public static final TagKey<Item> DEVELOPED_FILM_ROLLS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("developed_film_rolls"));
            public static final TagKey<Item> CYAN_PRINTING_DYES = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("cyan_printing_dyes"));
            public static final TagKey<Item> MAGENTA_PRINTING_DYES = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("magenta_printing_dyes"));
            public static final TagKey<Item> YELLOW_PRINTING_DYES = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("yellow_printing_dyes"));
            public static final TagKey<Item> BLACK_PRINTING_DYES = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("black_printing_dyes"));
            public static final TagKey<Item> PHOTO_PAPERS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("photo_papers"));
            public static final TagKey<Item> PHOTO_AGERS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("photo_agers"));
            public static final TagKey<Item> FLASHES = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("flashes"));
            public static final TagKey<Item> LENSES = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("lenses"));
            public static final TagKey<Item> FILTERS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("filters"));

            public static final TagKey<Item> RED_FILTERS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("red_filters"));
            public static final TagKey<Item> GREEN_FILTERS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("green_filters"));
            public static final TagKey<Item> BLUE_FILTERS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Exposure.resource("blue_filters"));
        }

        public static class Blocks {
            public static final TagKey<Block> CHROMATIC_REFRACTORS = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Exposure.resource("chromatic_refractors"));
        }

        public static class Entities {
            public static final TagKey<EntityType<?>> IGNORES_CAMERA = TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Exposure.resource("ignores_camera"));
        }
    }

    public static class ArgumentTypes {
        public static final Supplier<ArgumentTypeInfo<SizeMultiplierArgument, SingletonArgumentInfo<SizeMultiplierArgument>.Template>> EXPOSURE_SIZE =
                REGISTRAR.commandArgumentType("exposure_size", SizeMultiplierArgument.class, SingletonArgumentInfo.contextFree(SizeMultiplierArgument::new));
        public static final Supplier<ArgumentTypeInfo<ExposureLookArgument, SingletonArgumentInfo<ExposureLookArgument>.Template>> EXPOSURE_LOOK =
                REGISTRAR.commandArgumentType("exposure_look", ExposureLookArgument.class, SingletonArgumentInfo.contextFree(ExposureLookArgument::new));
        public static final Supplier<ArgumentTypeInfo<ShaderLocationArgument, SingletonArgumentInfo<ShaderLocationArgument>.Template>> SHADER_LOCATION =
                REGISTRAR.commandArgumentType("shader_location", ShaderLocationArgument.class, SingletonArgumentInfo.contextFree(ShaderLocationArgument::new));
        public static final Supplier<ArgumentTypeInfo<TextureLocationArgument, SingletonArgumentInfo<TextureLocationArgument>.Template>> TEXTURE_LOCATION =
                REGISTRAR.commandArgumentType("texture_location", TextureLocationArgument.class, SingletonArgumentInfo.contextFree(TextureLocationArgument::new));
        public static final Supplier<ArgumentTypeInfo<ColorPaletteArgument, SingletonArgumentInfo<ColorPaletteArgument>.Template>> COLOR_PALETTE_LOCATION =
                REGISTRAR.commandArgumentType("color_palette_location", ColorPaletteArgument.class, SingletonArgumentInfo.contextFree(ColorPaletteArgument::new));

        public static void init() {
        }
    }

    public static class Registries {
        public static final ResourceKey<Registry<ColorPalette>> COLOR_PALETTE = ResourceKey.createRegistryKey(Exposure.resource("color_palette"));
        public static final ResourceKey<Registry<Lens>> LENS = ResourceKey.createRegistryKey(Exposure.resource("lens"));
        public static final ResourceKey<Registry<Filter>> FILTER = ResourceKey.createRegistryKey(Exposure.resource("filter"));
    }
}
