package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.export.ImageExporter;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.gui.component.SteppedZoom;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.input.Modifier;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.world.photograph.PhotographType;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class PhotographScreen extends Screen {
    protected final Pager pager = new Pager()
            .setCycled(true)
            .setChangeSound(new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK))
            .onPageChanged(this::pageChanged);

    protected final SteppedZoom zoom = new SteppedZoom()
            .zoomInSteps(4)
            .zoomOutSteps(4)
            .zoomPerStep(1.4)
            .defaultZoom(1);

    protected final KeyBindings keyBindings = KeyBindings.of(
            Key.press(Minecrft.options().keyInventory).executes(this::onClose),
            Key.press(InputConstants.KEY_ADD).or(Key.press(InputConstants.KEY_EQUALS)).executes(zoom::zoomIn),
            Key.press(GLFW.GLFW_KEY_KP_SUBTRACT).or(Key.press(InputConstants.KEY_MINUS)).executes(zoom::zoomOut),
            Key.press(Modifier.CONTROL, InputConstants.KEY_I).executes(this::dropAsItem),
            Key.press(Modifier.CONTROL, InputConstants.KEY_C).executes(this::copyIdentifierToClipboard),
            Key.press(Modifier.CONTROL | Modifier.SHIFT, InputConstants.KEY_C).executes(this::copySavedFilePathToClipboard),
            Key.press(Modifier.CONTROL, InputConstants.KEY_S).executes(this::openSavedFile),
            Key.press(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::previousPage),
            Key.press(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::nextPage),
            Key.release(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::resetCooldown),
            Key.release(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::resetCooldown)
    );

    protected final PhotographProvider photographProvider;

    protected float x;
    protected float y;

    protected final Set<String> savedExposureIds = new HashSet<>();
    protected final Map<String, File> savedExposureFiles = new HashMap<>();

    protected ArrayList<ItemAndStack<PhotographItem>> photographs = new ArrayList<>();

    public PhotographScreen(PhotographProvider photographProvider) {
        super(Component.empty());
        this.photographProvider = photographProvider;
        setPhotographs(photographProvider.get());

        if (shouldQueryAllPhotographsImmediately()) {
            queryAllPhotographs(photographs);
        }
    }

    public PhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
        this(PhotographProvider.fixed(photographs));
    }

    protected void setPhotographs(List<ItemAndStack<PhotographItem>> photographs) {
        this.photographs.clear();
        this.photographs.addAll(photographs);
        this.pager.setPagesCount(photographs.size());
        this.pager.setPage(0);
    }

    @Override
    public void tick() {
        if (photographProvider.shouldRefresh()) {
            setPhotographs(photographProvider.get());
        }
    }

    @Override
    protected void init() {
        super.init();

        ImageButton previousButton = new ImageButton(0, (int) (height / 2f - 16 / 2f), 16, 16,
                Widgets.PREVIOUS_BUTTON_SPRITES,
                button -> pager.changePage(PagingDirection.PREVIOUS), Component.translatable("gui.exposure.previous_page"));
        addRenderableWidget(previousButton);

        ImageButton nextButton = new ImageButton(width - 16, (int) (height / 2f - 16 / 2f), 16, 16,
                Widgets.NEXT_BUTTON_SPRITES,
                button -> pager.changePage(PagingDirection.NEXT), Component.translatable("gui.exposure.next_page"));
        addRenderableWidget(nextButton);

        pager.setPreviousPageButton(previousButton)
             .setNextPageButton(nextButton);
    }

    protected boolean shouldQueryAllPhotographsImmediately() {
        return true;
    }

    protected void queryAllPhotographs(List<ItemAndStack<PhotographItem>> photographs) {
        for (ItemAndStack<PhotographItem> photograph : photographs) {
            photograph.getItem().getFrame(photograph.getItemStack())
                    .identifier()
                    .ifId(id -> ExposureClient.exposureStore().getOrRequest(id));
        }
    }

    public ItemAndStack<PhotographItem> getCurrentPhotograph() {
        return photographs.getFirst();
    }

    protected void pageChanged(int oldPage, int newPage) {
        int distance = newPage - oldPage;
        Collections.rotate(photographs, -distance);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float zoomFactor = height * 0.8f;
        float scale = (float) (zoom.get() * zoomFactor);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        renderTransparentBackground(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().translate(width / 2f, height / 2f, 50);
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.pose().translate(-0.5, -0.5, 0);

        MultiBufferSource.BufferSource bufferSource = Minecrft.get().renderBuffers().bufferSource();

        ExposureClient.photographRenderer().renderStackedPhotographs(photographs, guiGraphics.pose(), bufferSource,
                LightTexture.FULL_BRIGHT, 255, 255, 255, 255);

        bufferSource.endBatch();
        guiGraphics.pose().popPose();

        ItemAndStack<PhotographItem> photograph = getCurrentPhotograph();

        guiGraphics.pose().pushPose();
        // Places widgets above photograph, because they will be covered when photo is zoomed in
        guiGraphics.pose().translate(0, 0, 100);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderFrameInfoHint(guiGraphics, mouseX, mouseY, photograph);
        guiGraphics.pose().popPose();

        if (Config.Client.EXPORT_PHOTOGRAPH_WHEN_VIEWED.get()) {
            trySaveToFile(photograph);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Background is rendered manually in #render method.
        // Otherwise, background will be rendered on top of a photograph.
    }

    private void renderFrameInfoHint(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, ItemAndStack<PhotographItem> photograph) {
        if (Minecrft.get().player == null || !Minecrft.get().player.isCreative()) {
            return;
        }

        Frame frame = photograph.getItem().getFrame(photograph.getItemStack());
        if (frame == Frame.EMPTY) {
            return;
        }

        guiGraphics.drawString(font, "?", width - font.width("?") - 10, 10, 0xFFFFFFFF);

        if (mouseX > width - 20 && mouseX < width && mouseY < 20) {
            String exposureName = frame.identifier().map(id -> id, ResourceLocation::toString);

            List<Component> lines = new ArrayList<>();

            lines.add(Component.literal(exposureName));
            lines.add(Component.translatable("gui.exposure.photograph_screen.drop_as_item_tooltip", Component.literal("CTRL + I")));
            lines.add(Component.translatable("gui.exposure.photograph_screen.copy_" +
                    frame.identifier().map(id -> "id", texture -> "texture_path") + "_tooltip", "CTRL + C"));

            frame.identifier().getId().ifPresent(id -> {
                if (savedExposureFiles.containsKey(id)) {
                    lines.add(Component.translatable("gui.exposure.photograph_screen.copy_saved_file_path_tooltip", Component.literal("CTRL + SHIFT + C")));
                    lines.add(Component.translatable("gui.exposure.photograph_screen.open_saved_file_tooltip", Component.literal("CTRL + S")));
                }
            });

            guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY + 20);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyBindings.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return keyBindings.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;

        if (scrollY >= 0.0) {
            zoom.zoomIn();
        } else {
            zoom.zoomOut();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            float centerX = width / 2f;
            float centerY = height / 2f;
            x = (float) Mth.clamp(x + dragX, -centerX, centerX);
            y = (float) Mth.clamp(y + dragY, -centerY, centerY);
            return true;
        }

        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // --

    protected boolean dropAsItem() {
        if (!Minecrft.playerOrThrow().isCreative()) {
            return false;
        }
        ItemStack droppedStack = getCurrentPhotograph().getItemStack().copy();

        // Set type so copying recipe works properly.
        Frame frame = droppedStack.getOrDefault(Exposure.DataComponents.PHOTOGRAPH_FRAME, Frame.EMPTY);
        ExposureType type = frame.type();
        droppedStack.set(Exposure.DataComponents.PHOTOGRAPH_TYPE, type);

        Minecrft.gameModeOrThrow().handleCreativeModeItemDrop(droppedStack);
        Minecrft.playerOrThrow().displayClientMessage(Component.translatable("gui.exposure.photograph_screen.item_dropped_message",
                droppedStack.getDisplayName()), false);
        return true;
    }

    protected boolean copyIdentifierToClipboard() {
        Frame frame = getCurrentPhotograph().map(PhotographItem::getFrame);
        if (!Minecrft.playerOrThrow().isCreative() || frame.equals(Frame.EMPTY)) {
            return false;
        }
        String text = frame.identifier().map(id -> id, ResourceLocation::toString);
        Minecrft.get().keyboardHandler.setClipboard(text);
        Minecrft.playerOrThrow().displayClientMessage(
                Component.translatable("gui.exposure.photograph_screen.copied_message", text), false);
        return true;
    }

    protected boolean copySavedFilePathToClipboard() {
        return getCurrentPhotograph()
                .map(PhotographItem::getFrame)
                .identifier()
                .mapId(id -> {
                    if (savedExposureFiles.get(id) instanceof File file) {
                        Minecrft.get().keyboardHandler.setClipboard(file.getAbsolutePath());
                        Minecrft.playerOrThrow().displayClientMessage(
                                Component.translatable("gui.exposure.photograph_screen.copied_message", file.getAbsolutePath()), false);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }

    protected boolean openSavedFile() {
        return getCurrentPhotograph()
                .map(PhotographItem::getFrame)
                .identifier()
                .mapId(id -> {
                    if (savedExposureFiles.get(id) instanceof File file) {
                        Util.getPlatform().openFile(file);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }

    // --

    protected void trySaveToFile(ItemAndStack<PhotographItem> photograph) {
        Frame frame = photograph.getItem().getFrame(photograph.getItemStack());

        if (frame == Frame.EMPTY || !frame.identifier().isId() || !frame.isTakenBy(Minecrft.playerOrThrow())) {
            return;
        }

        String id = frame.identifier().getId().orElseThrow();

        PhotographType photographType = photograph.getItem().getType(photograph.getItemStack());
        PhotographStyle photographStyle = PhotographStyle.of(photograph.getItemStack());

        String filename = getFilename(id, photographType);

        if (savedExposureIds.contains(filename)) {
            return;
        }

        ExposureClient.exposureStore().getOrRequest(id).getData().ifPresent(exposure -> {
            savedExposureIds.add(filename);

            CompletableFuture.runAsync(() -> new ImageExporter(exposure, filename)
                            .modify(ImageEffect.chain(
                                    photographStyle.modifier(),
                                    ImageEffect.Resize.multiplier(Config.Client.EXPORT_SIZE_MULTIPLIER.get())
                            ))
                            .toExposuresFolder()
                            .organizeByWorld(Config.Client.EXPORT_ORGANIZE_BY_WORLD.get())
                            .setCreationDate(exposure.getTag().unixTimestamp())
                            .onExport(file -> savedExposureFiles.put(id, file))
                            .export())
                    .handle((unused, throwable) -> {
                        Exposure.LOGGER.error(throwable.getMessage());
                        return null;
                    });
        });
    }

    protected @NotNull String getFilename(String id, PhotographType photographType) {
        String suffix = photographType.getFileSuffix();
        if (!StringUtil.isNullOrEmpty(suffix)) {
            return id + "_" + suffix;
        }
        return id;
    }

    public interface PhotographProvider {
        boolean shouldRefresh();

        /**
         * Should have at least one photograph.
         */
        List<ItemAndStack<PhotographItem>> get();

        static PhotographProvider fixed(List<ItemAndStack<PhotographItem>> photographs) {
            Preconditions.checkState(!photographs.isEmpty(), "No photographs to display.");
            return new PhotographProvider() {
                private final List<ItemAndStack<PhotographItem>> list = photographs;

                @Override
                public boolean shouldRefresh() {
                    return false;
                }

                @Override
                public List<ItemAndStack<PhotographItem>> get() {
                    return list;
                }
            };
        }

        static PhotographProvider fromPhotographItem(int slot) {
            return new ItemProvider(() -> Minecrft.playerOrThrow().getInventory().getItem(slot));
        }

        class ItemProvider implements PhotographProvider {
            protected Supplier<ItemStack> itemSupplier;
            protected List<ItemAndStack<PhotographItem>> photographs;

            public ItemProvider(Supplier<ItemStack> itemSupplier) {
                this.itemSupplier = itemSupplier;
                ItemStack stack = itemSupplier.get();
                Preconditions.checkState(stack.getItem() instanceof PhotographItem || stack.getItem() instanceof StackedPhotographsItem,
                        "itemSupplier should supply valid Photograph or Stacked Photographs item stack at the moment of creation.");
                this.photographs = fromItemStack(stack);
            }

            protected List<ItemAndStack<PhotographItem>> fromItemStack(ItemStack stack) {
                if (stack.getItem() instanceof PhotographItem) {
                    return List.of(new ItemAndStack<>(stack));
                }

                if (stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem) {
                    return stackedPhotographsItem.getPhotographs(stack).photographsItemAndStacks();
                }

                return Collections.emptyList();
            }

            @Override
            public boolean shouldRefresh() {
                ItemStack item = itemSupplier.get();
                List<ItemAndStack<PhotographItem>> newPhotographs = fromItemStack(item);

                if (newPhotographs.isEmpty()) {
                    return false;
                }

                boolean shouldRefresh = !get().equals(newPhotographs);
                if (shouldRefresh) {
                    photographs = newPhotographs;
                }
                return shouldRefresh;
            }

            @Override
            public List<ItemAndStack<PhotographItem>> get() {
                return photographs;
            }
        }
    }
}