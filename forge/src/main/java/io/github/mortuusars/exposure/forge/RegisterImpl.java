package io.github.mortuusars.exposure.forge;

import io.github.mortuusars.exposure.Exposure;
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
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class RegisterImpl {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Exposure.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Exposure.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Exposure.ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Exposure.ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Exposure.ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Exposure.ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Exposure.ID);

    public static <T extends Block> Supplier<T> block(String id, Supplier<T> supplier) {
        return BLOCKS.register(id, supplier);
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> blockEntityType(String id, Supplier<T> sup) {
        return BLOCK_ENTITY_TYPES.register(id, sup);
    }

    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(Register.BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        return BlockEntityType.Builder.of(blockEntitySupplier::create, validBlocks).build(null);
    }

    public static <T extends Item> Supplier<T> item(String id, Supplier<T> supplier) {
        return ITEMS.register(id, supplier);
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> factory, MobCategory category,
                                                                        float width, float height, int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        return ENTITY_TYPES.register(id, () -> EntityType.Builder.of(factory, category)
                .sized(width, height)
                .clientTrackingRange(clientTrackingRange)
                .setShouldReceiveVelocityUpdates(velocityUpdates)
                .updateInterval(updateInterval)
                .build(id));
    }

    public static <T extends SoundEvent> Supplier<T> soundEvent(String id, Supplier<T> supplier) {
        return SOUND_EVENTS.register(id, supplier);
    }

    public static <T extends MenuType<E>, E extends AbstractContainerMenu> Supplier<MenuType<E>> menuType(String id, Register.MenuTypeSupplier<E> supplier) {
        return MENU_TYPES.register(id, () -> IForgeMenuType.create(supplier::create));
    }

    public static Supplier<RecipeSerializer<?>> recipeSerializer(String id, Supplier<RecipeSerializer<?>> supplier) {
        return RECIPE_SERIALIZERS.register(id, supplier);
    }
}
