package io.github.mortuusars.exposure.menu;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.item.AlbumItem;
import io.github.mortuusars.exposure.item.AlbumPage;
import io.github.mortuusars.exposure.item.PhotographItem;
import io.github.mortuusars.exposure.item.SignedAlbumItem;
import io.github.mortuusars.exposure.util.ItemAndStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class AlbumMenu extends AbstractContainerMenu {
    public enum Page {
        LEFT, RIGHT;

        public int getPageIndexFromSpread(int spreadIndex) {
            return spreadIndex * 2 + (this == LEFT ? 0 : 1);
        }
    }

    public static final int MAX_PAGES = 16;

    public static final int CANCEL_ADDING_PHOTO_BUTTON = -1;
    public static final int PREVIOUS_PAGE_BUTTON = 0;
    public static final int NEXT_PAGE_BUTTON = 1;
    public static final int LEFT_PAGE_PHOTO_BUTTON = 2;
    public static final int RIGHT_PAGE_PHOTO_BUTTON = 3;

    private final ItemAndStack<AlbumItem> album;

    private final List<AlbumPage> pages;

    private final List<AlbumPhotographSlot> photographSlots = new ArrayList<>();
    private final List<AlbumPlayerInventorySlot> playerInventorySlots = new ArrayList<>();

    private final DataSlot currentSpreadIndex = DataSlot.standalone();

    @Nullable
    private Page pageBeingAddedTo = null;

    private final Map<Integer, Consumer<Player>> buttonActions = new HashMap<>() {{
        put(CANCEL_ADDING_PHOTO_BUTTON, p -> {
            pageBeingAddedTo = null;
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
        put(LEFT_PAGE_PHOTO_BUTTON, p -> onPhotoButtonPress(p, Page.LEFT));
        put(RIGHT_PAGE_PHOTO_BUTTON, p -> onPhotoButtonPress(p, Page.RIGHT));
    }};

    public AlbumMenu(int containerId, Inventory playerInventory, ItemAndStack<AlbumItem> album) {
        super(Exposure.MenuTypes.ALBUM.get(), containerId);
        this.album = album;

        List<AlbumPage> albumPages = album.getItem().getPages(album.getStack());
        pages = isAlbumEditable() ? new ArrayList<>(albumPages) : albumPages;

        if (isAlbumEditable()) {
            while (pages.size() < AlbumMenu.MAX_PAGES) {
                addEmptyPage();
            }
        }

        addPhotographSlots();
        addPlayerInventorySlots(playerInventory, 70, 115);
        addDataSlot(currentSpreadIndex);

        /* give Dev exposure:album{Pages:[
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"view1"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"Dev_30717"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"Dev_28958"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Texture:"exposure:textures/block/flash.png"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"Dev_1043"}}},
            {Photo:{}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"Dev_6323"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Texture:"exposure:textures/block/lightroom_side.png"}}},
            {Photo:{id:"exposure:photograph",Count:1,tag:{Id:"Dev_1043"}}},
        ]}
         */
    }

    public boolean isAlbumEditable() {
        return !(album.getItem() instanceof SignedAlbumItem);
    }

    protected void addEmptyPage() {
        AlbumPage page = new AlbumPage(ItemStack.EMPTY, Collections.emptyList());
        pages.add(page);
        album.getItem().addPage(album.getStack(), page);
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

        container.addListener(this::updateAlbumPages);
    }

    private void updateAlbumPages(Container container) {
        List<AlbumPage> pages = getPages();
        for (int i = 0; i < pages.size(); i++) {
            AlbumPage page = pages.get(i);
            ItemStack stack = container.getItem(i);
            page.setPhotographStack(stack);
            album.getItem().setPhotoOnPage(album.getStack(), stack, i);
        }
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
            AlbumPlayerInventorySlot slot = new AlbumPlayerInventorySlot(playerInventory, index,
                    x + index * 18, y + 58);
            addSlot(slot);
            playerInventorySlots.add(slot);
        }
    }

    private void onPhotoButtonPress(Player player, Page page) {
        Preconditions.checkArgument(isAlbumEditable(),
                "Photo Button should be disabled and hidden when Album is not editable. " + album.getStack());

        Optional<AlbumPhotographSlot> photographSlot = getPhotographSlot(page);
        if (photographSlot.isEmpty())
            return;

        AlbumPhotographSlot slot = photographSlot.get();
        if (!slot.hasItem()) {
            pageBeingAddedTo = page;
        }
        else {
            ItemStack stack = slot.getItem();
            if (!player.getInventory().add(stack))
                player.drop(stack, false);

            slot.set(ItemStack.EMPTY);
        }

        updatePlayerInventorySlots();
    }

    public List<AlbumPhotographSlot> getPhotographSlots() {
        return photographSlots;
    }

    public List<AlbumPlayerInventorySlot> getPlayerInventorySlots() {
        return playerInventorySlots;
    }

    public List<AlbumPage> getPages() {
        return pages;
    }

    public Optional<AlbumPage> getLeftPage() {
        return getPage(getCurrentSpreadIndex() * 2);
    }

    public Optional<AlbumPage> getRightPage() {
        return getPage(getCurrentSpreadIndex() * 2 + 1);
    }

    public Optional<AlbumPage> getPage(int pageIndex) {
        if (pageIndex <= getPages().size() - 1)
            return Optional.ofNullable(getPages().get(pageIndex));

        return Optional.empty();
    }

    public Optional<AlbumPhotographSlot> getPhotographSlot(Page page) {
        return getPhotographSlot(getCurrentSpreadIndex() * 2 + (page == Page.LEFT ? 0 : 1));
    }

    public Optional<AlbumPhotographSlot> getPhotographSlot(int index) {
        if (index <= photographSlots.size() - 1)
            return Optional.ofNullable(photographSlots.get(index));

        return Optional.empty();
    }

    public int getCurrentSpreadIndex() {
        return this.currentSpreadIndex.get();
    }

    public void setCurrentSpreadIndex(int spreadIndex) {
        this.currentSpreadIndex.set(spreadIndex);
    }

    public boolean isInAddingPhotographMode() {
        return getPageBeingAddedTo() != null;
    }

    public @Nullable Page getPageBeingAddedTo() {
        return pageBeingAddedTo;
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

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Both sides

        if (pageBeingAddedTo == null || slotId < 0 || slotId >= slots.size())
            return;

        Slot slot = slots.get(slotId);
        ItemStack stack = slot.getItem();

        if (button == InputConstants.MOUSE_BUTTON_LEFT
                && slot instanceof AlbumPlayerInventorySlot
                && stack.getItem() instanceof PhotographItem) {
            int pageIndex = pageBeingAddedTo.getPageIndexFromSpread(getCurrentSpreadIndex());
            Optional<AlbumPhotographSlot> photographSlot = getPhotographSlot(pageIndex);
            if (photographSlot.isEmpty() || !photographSlot.get().getItem().isEmpty())
                return;

            photographSlot.get().set(stack);
            slot.set(ItemStack.EMPTY);

            pageBeingAddedTo = null;
            updatePlayerInventorySlots();
            return;
        }

        if (isInAddingPhotographMode())
            super.clicked(slotId, button, clickType, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().getItem() instanceof AlbumItem
                || player.getOffhandItem().getItem() instanceof AlbumItem;
    }

    public static AlbumMenu fromBuffer(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new AlbumMenu(containerId, inventory, new ItemAndStack<>(buffer.readItem()));
    }

    protected void updatePlayerInventorySlots() {
        boolean isInAddingPhotographMode = isInAddingPhotographMode();
        for (AlbumPlayerInventorySlot slot : playerInventorySlots) {
            slot.setActive(isInAddingPhotographMode);
        }
    }
}
