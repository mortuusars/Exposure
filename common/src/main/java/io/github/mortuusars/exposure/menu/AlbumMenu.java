package io.github.mortuusars.exposure.menu;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.item.AlbumPage;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.server.AlbumSignC2SP;
import io.github.mortuusars.exposure.util.ItemAndStack;
import io.github.mortuusars.exposure.util.Side;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class AlbumMenu extends AbstractContainerMenu {
    public static final int CANCEL_ADDING_PHOTO_BUTTON = -1;
    public static final int PREVIOUS_PAGE_BUTTON = 0;
    public static final int NEXT_PAGE_BUTTON = 1;
    public static final int LEFT_PAGE_PHOTO_BUTTON = 2;
    public static final int RIGHT_PAGE_PHOTO_BUTTON = 3;
    public static final int ENTER_SIGN_MODE_BUTTON = 4;
    public static final int SIGN_BUTTON = 5;
    public static final int CANCEL_SIGNING_BUTTON = 6;

    protected final ItemAndStack<AlbumItem> album;
    protected final boolean editable;

    protected final List<AlbumPage> pages;

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

    public AlbumMenu(int containerId, Inventory playerInventory, ItemAndStack<AlbumItem> album, boolean editable) {
        this(Exposure.MenuTypes.ALBUM.get(), containerId, playerInventory, album, editable);
    }

    protected AlbumMenu(MenuType<? extends AbstractContainerMenu> type, int containerId, Inventory playerInventory, ItemAndStack<AlbumItem> album, boolean editable) {
        super(type, containerId);
        this.album = album;
        this.editable = editable;

        List<AlbumPage> albumPages = album.getItem().getPages(album.getStack());
        pages = isAlbumEditable() ? new ArrayList<>(albumPages) : albumPages;

        if (isAlbumEditable()) {
            while (pages.size() < album.getItem().getMaxPages()) {
                addEmptyPage();
            }
        }

        addPhotographSlots();
        addPlayerInventorySlots(playerInventory, 70, 115);
        addDataSlot(currentSpreadIndex);
    }

    private void addPhotographSlots() {
        ItemStack[] photographs = pages.stream().map(AlbumPage::getPhotographStack).toArray(ItemStack[]::new);
        SimpleContainer container = new SimpleContainer(photographs);

        for (int i = 0; i < container.getContainerSize(); i++) {
            int x = i % 2 == 0 ? 71 : 212;
            int y = 67;
            AlbumPhotographSlot slot = new AlbumPhotographSlot(container, i, x, y);
            addSlot(slot);
            photographSlots.add(slot);
        }

        container.addListener(c -> {
            List<AlbumPage> pages = getPages();
            for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
                AlbumPage page = pages.get(pageIndex);
                ItemStack stack = container.getItem(pageIndex);
                page.setPhotographStack(stack);
            }
            updateAlbumStack();
        });
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
            boolean disabled = index == playerInventory.selected && playerInventory.getSelected().getItem() instanceof AlbumItem;
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

    public boolean isAlbumEditable() {
        return editable;
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

    public void setTitle(String authorName) {
        this.title = authorName;
    }

    public boolean canSignAlbum() {
        for (AlbumPage page : getPages()) {
            if (!page.getPhotographStack().isEmpty() || page.getNote().left().map(note -> !note.isEmpty()).orElse(false))
                return true;
        }
        return false;
    }

    protected void signAlbum(Player player) {
        if (!player.level().isClientSide)
            return;

        if (!canSignAlbum())
            throw new IllegalStateException("Cannot sign the album.\n" + Arrays.toString(getPages().toArray()));

        Packets.sendToServer(new AlbumSignC2SP(title));
    }

    public void updateAlbumStack() {
        List<AlbumPage> pages = getPages();
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            AlbumPage page = pages.get(pageIndex);
            album.getItem().setPage(album.getStack(), page, pageIndex);
        }
    }

    protected void addEmptyPage() {
        AlbumPage page = album.getItem().createEmptyPage();
        pages.add(page);
        album.getItem().addPage(album.getStack(), page);
    }

    public List<AlbumPlayerInventorySlot> getPlayerInventorySlots() {
        return playerInventorySlots;
    }

    public List<AlbumPage> getPages() {
        return pages;
    }

    public Optional<AlbumPage> getPage(Side side) {
        return getPage(getCurrentSpreadIndex() * 2 + side.getIndex());
    }

    public Optional<AlbumPage> getPage(int pageIndex) {
        if (pageIndex <= getPages().size() - 1)
            return Optional.ofNullable(getPages().get(pageIndex));

        return Optional.empty();
    }

    public Optional<AlbumPhotographSlot> getPhotographSlot(Side side) {
        return getPhotographSlot(getCurrentSpreadIndex() * 2 + (side == Side.LEFT ? 0 : 1));
    }

    public Optional<AlbumPhotographSlot> getPhotographSlot(int index) {
        if (index <= photographSlots.size() - 1)
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
                "Photo Button should be disabled and hidden when Album is not editable. " + album.getStack());

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
                && stack.getItem() instanceof PhotographItem) {
            int pageIndex = getCurrentSpreadIndex() * 2 + sideBeingAddedTo.getIndex();
            Optional<AlbumPhotographSlot> photographSlot = getPhotographSlot(pageIndex);
            if (photographSlot.isEmpty() || !photographSlot.get().getItem().isEmpty())
                return;

            photographSlot.get().set(stack);
            slot.set(ItemStack.EMPTY);

            if (player.level().isClientSide)
                player.playSound(Exposure.SoundEvents.PHOTOGRAPH_PLACE.get(), 0.8f, 1.1f);

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
        return !isAlbumEditable() || (player.getMainHandItem().getItem() instanceof AlbumItem
                || player.getOffhandItem().getItem() instanceof AlbumItem);
    }

    public static AlbumMenu fromBuffer(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new AlbumMenu(containerId, inventory, new ItemAndStack<>(buffer.readItem()), buffer.readBoolean());
    }
}
