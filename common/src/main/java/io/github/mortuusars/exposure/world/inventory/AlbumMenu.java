package io.github.mortuusars.exposure.world.inventory;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.inventory.slot.AlbumPhotographSlot;
import io.github.mortuusars.exposure.world.inventory.slot.AlbumPlayerInventorySlot;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.component.album.AlbumContent;
import io.github.mortuusars.exposure.world.item.component.album.AlbumPage;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.AlbumSignC2SP;
import io.github.mortuusars.exposure.util.Side;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class AlbumMenu extends AbstractContainerMenu {
    public static final int CANCEL_ADDING_PHOTO_BUTTON = -1;
    public static final int PREVIOUS_PAGE_BUTTON = PagingDirection.PREVIOUS.ordinal();
    public static final int NEXT_PAGE_BUTTON = PagingDirection.NEXT.ordinal();
    public static final int LEFT_PAGE_PHOTO_BUTTON = 2;
    public static final int RIGHT_PAGE_PHOTO_BUTTON = 3;
    public static final int ENTER_SIGN_MODE_BUTTON = 4;
    public static final int SIGN_BUTTON = 5;
    public static final int CANCEL_SIGNING_BUTTON = 6;

    protected final int albumSlot;
    protected final ItemStack albumStack;
    protected final AlbumItem albumItem;

    protected final List<AlbumPhotographSlot> photographSlots = new ArrayList<>();
    protected final List<AlbumPlayerInventorySlot> playerInventorySlots = new ArrayList<>();

    protected DataSlot currentSpreadIndex = DataSlot.standalone();

    @Nullable
    protected Side sideBeingAddedTo = null;
    protected boolean signing;
    protected String title = "";

    protected final Map<Integer, Consumer<Player>> buttonActions = new HashMap<>() {{
        put(CANCEL_ADDING_PHOTO_BUTTON, p -> {
            sideBeingAddedTo = null;
            if (!getCarried().isEmpty()) {
                p.getInventory().placeItemBackInInventory(getCarried());
                setCarried(ItemStack.EMPTY);
            }
            updatePlayerInventorySlots();
        });
        put(PREVIOUS_PAGE_BUTTON, p -> {
            clickMenuButton(p, CANCEL_ADDING_PHOTO_BUTTON);
            setCurrentSpreadIndex(Math.max(0, getCurrentSpreadIndex() - 1));
        });
        put(NEXT_PAGE_BUTTON, p -> {
            clickMenuButton(p, CANCEL_ADDING_PHOTO_BUTTON);
            setCurrentSpreadIndex(Math.min((getPages().size() - 1) / 2, getCurrentSpreadIndex() + 1));
        });
        put(LEFT_PAGE_PHOTO_BUTTON, p -> onPhotoButtonPress(p, Side.LEFT));
        put(RIGHT_PAGE_PHOTO_BUTTON, p -> onPhotoButtonPress(p, Side.RIGHT));
        put(ENTER_SIGN_MODE_BUTTON, p -> {
            signing = true;
            sideBeingAddedTo = null;
        });
        put(SIGN_BUTTON, p -> signAlbum(p));
        put(CANCEL_SIGNING_BUTTON, p -> signing = false);
    }};

    public AlbumMenu(int containerId, Inventory playerInventory, int albumSlot) {
        this(Exposure.MenuTypes.ALBUM.get(), containerId, playerInventory, albumSlot);
    }

    protected AlbumMenu(MenuType<? extends AbstractContainerMenu> type, int containerId, Inventory playerInventory, int albumSlot) {
        super(type, containerId);
        this.albumSlot = albumSlot;

        albumStack = playerInventory.getItem(albumSlot);
        if (!(albumStack.getItem() instanceof AlbumItem item)) {
            throw new IllegalStateException("Expected AlbumItem in slot '" + albumSlot + "'. Got: " + albumStack);
        }

        this.albumItem = item;

        if (isAlbumEditable() && item.getContent(albumStack).pages().size() < AlbumContent.MAX_PAGES) {
            // Sets all pages to empty.
            item.setContent(albumStack, item.getContent(albumStack).toMutable().setPage(AlbumContent.MAX_PAGES - 1, AlbumPage.EMPTY).toImmutable());
        }

        addPhotographSlots();
        addPlayerInventorySlots(playerInventory, 70, 115);
        addDataSlot(currentSpreadIndex);
    }

    protected void addPhotographSlots() {
        ItemStack[] photographs = albumItem.getContent(albumStack).pages().stream().map(AlbumPage::photograph).toArray(ItemStack[]::new);
        SimpleContainer container = new SimpleContainer(photographs);

        for (int i = 0; i < container.getContainerSize(); i++) {
            int x = i % 2 == 0 ? 71 : 212;
            int y = 67;
            AlbumPhotographSlot slot = new AlbumPhotographSlot(container, i, x, y) {
                @Override
                public void set(ItemStack stack) {
                    super.set(stack);
                    onPhotographSlotChanged(getContainerSlot(), stack);
                }
            };
            addSlot(slot);
            photographSlots.add(slot);
        }
    }

    private void onPhotographSlotChanged(int slotIndex, ItemStack stack) {
        albumItem.updatePage(albumStack, slotIndex, page -> page.orElse(AlbumPage.EMPTY).setPhotograph(stack));
//        List<AlbumPage> pages = getPages();
//        AlbumPage page = pages.get(slotIndex);
//        page = page.setPhotograph(stack);
//        pages.set(slotIndex, page);
    }

    private void addPlayerInventorySlots(Inventory playerInventory, int x, int y) {
        // Player inventory slots
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                AlbumPlayerInventorySlot slot = new AlbumPlayerInventorySlot(playerInventory, column + row * 9 + 9,
                        x + column * 18, y + row * 18);
                addSlot(slot);
                playerInventorySlots.add(slot);
            }
        }

        // Player hotbar slots
        // Hotbar should go after main inventory for Shift+Click to work properly.
        for (int index = 0; index < 9; ++index) {
            boolean disabled = index == playerInventory.getSelectedSlot() && playerInventory.getSelectedItem().getItem() instanceof AlbumItem;
            AlbumPlayerInventorySlot slot = new AlbumPlayerInventorySlot(playerInventory, index,
                    x + index * 18, y + 58) {
                @Override
                public boolean mayPickup(Player player) {
                    return !disabled;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !disabled;
                }
            };
            addSlot(slot);
            playerInventorySlots.add(slot);
        }
    }

    protected void updatePlayerInventorySlots() {
        boolean isInAddingPhotographMode = isInAddingPhotographMode();
        for (AlbumPlayerInventorySlot slot : playerInventorySlots) {
            slot.setActive(isInAddingPhotographMode);
        }
    }

    public int getAlbumSlot() {
        return albumSlot;
    }

    public boolean isAlbumEditable() {
        return true;
    }

    public boolean isInAddingPhotographMode() {
        return getSideBeingAddedTo() != null;
    }

    public boolean isInSigningMode() {
        return signing;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean canSignAlbum() {
        for (AlbumPage page : getPages()) {
            if (!page.photograph().isEmpty() || !page.note().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void signAlbum(Player player) {
        if (!player.level().isClientSide()) {
            return;
        }

        if (!canSignAlbum()) {
            throw new IllegalStateException("Cannot sign the album.\n" + Arrays.toString(getPages().toArray()));
        }

        Packets.sendToServer(new AlbumSignC2SP(albumSlot, title, player.getScoreboardName()));
    }

    public List<AlbumPlayerInventorySlot> getPlayerInventorySlots() {
        return playerInventorySlots;
    }

    public List<AlbumPage> getPages() {
        return albumItem.getContent(albumStack).pages();
    }

    public Optional<AlbumPage> getPage(int pageIndex) {
        if (pageIndex <= getPages().size() - 1)
            return Optional.ofNullable(getPages().get(pageIndex));

        return Optional.empty();
    }

    public Optional<AlbumPage> getPage(Side side) {
        return getPage(getCurrentSpreadIndex() * 2 + side.getIndex());
    }

    public void updatePage(int pageIndex, Function<AlbumPage, AlbumPage> pageTransform) {
        albumItem.updatePage(albumStack, pageIndex, page -> pageTransform.apply(page.orElse(AlbumPage.EMPTY)));
    }

    public Optional<AlbumPhotographSlot> getPhotographSlot(Side side) {
        return getPhotographSlot(getCurrentSpreadIndex() * 2 + (side == Side.LEFT ? 0 : 1));
    }

    public Optional<AlbumPhotographSlot> getPhotographSlot(int index) {
        if (index >= 0 && index < photographSlots.size())
            return Optional.ofNullable(photographSlots.get(index));

        return Optional.empty();
    }

    public ItemStack getPhotograph(Side side) {
        return getPhotographSlot(side).map(Slot::getItem).orElse(ItemStack.EMPTY);
    }

    public int getCurrentSpreadIndex() {
        return this.currentSpreadIndex.get();
    }

    public void setCurrentSpreadIndex(int spreadIndex) {
        this.currentSpreadIndex.set(spreadIndex);
    }

    public @Nullable Side getSideBeingAddedTo() {
        return sideBeingAddedTo;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        @Nullable Consumer<Player> buttonAction = buttonActions.get(id);
        if (buttonAction != null) {
            buttonAction.accept(player);
            return true;
        }

        return false;
    }

    private void onPhotoButtonPress(Player player, Side side) {
        Preconditions.checkArgument(isAlbumEditable(),
                "Photo Button should be disabled and hidden when Album is not editable. " + albumStack);

        Optional<AlbumPhotographSlot> photographSlot = getPhotographSlot(side);
        if (photographSlot.isEmpty())
            return;

        AlbumPhotographSlot slot = photographSlot.get();
        if (!slot.hasItem()) {
            sideBeingAddedTo = side;
        }
        else {
            ItemStack stack = slot.getItem();
            if (!player.getInventory().add(stack))
                player.drop(stack, false);

            slot.set(ItemStack.EMPTY);
        }

        updatePlayerInventorySlots();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Both sides

        if (sideBeingAddedTo == null || slotId < 0 || slotId >= slots.size()) {
            super.clicked(slotId, button, clickType, player);
            return;
        }

        Slot slot = slots.get(slotId);
        ItemStack stack = slot.getItem();

        if (button == InputConstants.MOUSE_BUTTON_LEFT
                && slot instanceof AlbumPlayerInventorySlot
                && stack.getItem() instanceof PhotographItem
                && getCarried().isEmpty()) {
            int pageIndex = getCurrentSpreadIndex() * 2 + sideBeingAddedTo.getIndex();
            Optional<AlbumPhotographSlot> photographSlot = getPhotographSlot(pageIndex);
            if (photographSlot.isEmpty() || !photographSlot.get().getItem().isEmpty())
                return;

            photographSlot.get().set(stack);
            slot.set(ItemStack.EMPTY);

            if (player.level().isClientSide()) {
                player.playSound(Exposure.SoundEvents.PHOTOGRAPH_PLACE.get(), 0.8f, 1.1f);
            }

            sideBeingAddedTo = null;
            updatePlayerInventorySlots();
        }
        else
            super.clicked(slotId, button, clickType, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().getItem(albumSlot).getItem() instanceof AlbumItem;
    }

    public static AlbumMenu fromBuffer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        return new AlbumMenu(containerId, playerInventory, buffer.readVarInt());
    }
}
