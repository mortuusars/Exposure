package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Register;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Supplier;

public class RegisterImpl {
    public static <T extends Block> Supplier<T> block(String id, Supplier<T> supplier) {
        throw new NotImplementedException();
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> blockEntityType(String id, Supplier<T> sup) {
        throw new NotImplementedException();
    }

    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(Register.BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        throw new NotImplementedException();
    }

    public static <T extends Item> Supplier<T> item(String id, Supplier<T> supplier) {
        throw new NotImplementedException();
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> factory,
                                                                        MobCategory category, float width, float height,
                                                                        int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        throw new NotImplementedException();
    }

    public static <T extends SoundEvent> Supplier<T> soundEvent(String id, Supplier<T> supplier) {
        throw new NotImplementedException();
    }

    public static <T extends MenuType<E>, E extends AbstractContainerMenu> Supplier<MenuType<E>> menuType(String id, Register.MenuTypeSupplier<E> supplier) {
        throw new NotImplementedException();
    }

    public static Supplier<RecipeSerializer<?>> recipeSerializer(String id, Supplier<RecipeSerializer<?>> supplier) {
        throw new NotImplementedException();
    }
}
