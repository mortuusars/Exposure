package io.github.mortuusars.exposure;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class Register {
    @ExpectPlatform
    public static <T extends Block> Supplier<T> block(String id, Supplier<T> supplier) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> blockEntityType(String id, Supplier<T> sup) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockEntitySupplier<T extends BlockEntity> {

        @NotNull T create(BlockPos pos, BlockState state);
    }

    @ExpectPlatform
    public static <T extends Item> Supplier<T> item(String id, Supplier<T> supplier) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> factory,
                                                                        MobCategory category, float width, float height,
                                                                        int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends SoundEvent> Supplier<T> soundEvent(String id, Supplier<T> supplier) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends MenuType<E>, E extends AbstractContainerMenu> Supplier<T> menuType(String id, MenuTypeSupplier<E> supplier) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface MenuTypeSupplier<T extends AbstractContainerMenu> {
        @NotNull T create(int windowId, Inventory playerInv, FriendlyByteBuf extraData);
    }

    @ExpectPlatform
    public static Supplier<RecipeSerializer<?>> recipeSerializer(String filmDeveloping, Supplier<RecipeSerializer<?>> supplier) {
        throw new AssertionError();
    }
}
