package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.Register;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class RegisterImpl {
    public static <T extends Block> Supplier<T> block(String id, Supplier<T> supplier) {
        T obj = Registry.register(BuiltInRegistries.BLOCK, Exposure.resource(id), supplier.get());
        return () -> obj;
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> blockEntityType(String id, Supplier<T> supplier) {
        T obj = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Exposure.resource(id), supplier.get());
        return () -> obj;
    }

    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(Register.BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        return FabricBlockEntityTypeBuilder.create(blockEntitySupplier::create, validBlocks).build();
    }

    public static <T extends Item> Supplier<T> item(String id, Supplier<T> supplier) {
        T obj = Registry.register(BuiltInRegistries.ITEM, Exposure.resource(id), supplier.get());
        return () -> obj;
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> factory,
                                                                        MobCategory category, float width, float height,
                                                                        int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        EntityType<T> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, Exposure.resource(id),
                FabricEntityTypeBuilder.create(category, factory)
                        .dimensions(EntityDimensions.fixed(width, height))
                        .trackRangeBlocks(clientTrackingRange)
                        .forceTrackedVelocityUpdates(velocityUpdates)
                        .trackedUpdateRate(updateInterval)
                        .build());
        return () -> type;
    }

    public static <T extends SoundEvent> Supplier<T> soundEvent(String id, Supplier<T> supplier) {
        T obj = Registry.register(BuiltInRegistries.SOUND_EVENT, Exposure.resource(id), supplier.get());
        return () -> obj;
    }

    public static <T extends MenuType<E>, E extends AbstractContainerMenu> Supplier<MenuType<E>> menuType(String id, Register.MenuTypeSupplier<E> supplier) {
        ExtendedScreenHandlerType<E> type = Registry.register(BuiltInRegistries.MENU, Exposure.resource(id), new ExtendedScreenHandlerType<>(supplier::create));
        return () -> type;
    }

    public static Supplier<RecipeSerializer<?>> recipeSerializer(String id, Supplier<RecipeSerializer<?>> supplier) {
        RecipeSerializer<?> obj = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Exposure.resource(id), supplier.get());
        return () -> obj;
    }
}
