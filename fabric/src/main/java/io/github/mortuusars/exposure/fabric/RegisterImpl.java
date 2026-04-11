package io.github.mortuusars.exposure.fabric;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.Register;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RegisterImpl {
    public static <T extends Block> Supplier<T> block(String id, Function<BlockBehaviour.Properties, T> blockFactory, Supplier<BlockBehaviour.Properties> supplier) {
        T obj = Registry.register(BuiltInRegistries.BLOCK, Exposure.resource(id),
                blockFactory.apply(supplier.get()
                        .setId(ResourceKey.create(Registries.BLOCK, Exposure.resource(id)
                        ))));
        return () -> obj;
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> blockEntityType(String id, Supplier<T> supplier) {
        T obj = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Exposure.resource(id), supplier.get());
        return () -> obj;
    }

    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(Register.BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        return new BlockEntityType<T>(blockEntitySupplier::create, Set.of(validBlocks));
    }

    public static <T extends Item> Supplier<T> item(String id, Function<Item.Properties, T> itemFactory, Supplier<Item.Properties> propertiesSupplier) {
        T obj = Registry.register(BuiltInRegistries.ITEM, Exposure.resource(id),
                itemFactory.apply(propertiesSupplier.get()
                        .setId(ResourceKey.create(Registries.ITEM, Exposure.resource(id)
                        ))));
        return () -> obj;
    }

    public static <T extends CreativeModeTab> Supplier<T> creativeTab(String id, Supplier<T> supplier) {
        T obj = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Exposure.resource(id), supplier.get());
        return () -> obj;
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> factory,
                                                                        MobCategory category, float width, float height,
                                                                        int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        EntityType<T> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, Exposure.resource(id),
                EntityType.Builder.of(factory, category)
                        .sized(width, height)
                        .clientTrackingRange(clientTrackingRange)
                        .alwaysUpdateVelocity(velocityUpdates)
                        .updateInterval(updateInterval)
                        .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Exposure.resource(id))));
        return () -> type;
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> factory, MobCategory category, boolean receiveVelocityUpdates, Consumer<EntityType.Builder<T>> typeBuilder) {
        EntityType.Builder<T> builder = EntityType.Builder.of(factory, category);
        typeBuilder.accept(builder);
        builder.alwaysUpdateVelocity(receiveVelocityUpdates);
        EntityType<T> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, Exposure.resource(id), builder.build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Exposure.resource(id))));
        return () -> type;
    }

    public static <T extends SoundEvent> Supplier<T> soundEvent(String id, Supplier<T> supplier) {
        T obj = Registry.register(BuiltInRegistries.SOUND_EVENT, Exposure.resource(id), supplier.get());
        return () -> obj;
    }

    public static <T extends MenuType<E>, E extends AbstractContainerMenu> Supplier<MenuType<E>> menuType(String id, Register.MenuTypeSupplier<E> supplier) {
        ExtendedScreenHandlerType<E, byte[]> type = new ExtendedScreenHandlerType<>((syncId, inventory, data) -> {
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess());
            E menu = supplier.create(syncId, inventory, buffer);
            buffer.release();
            return menu;
        }, ByteBufCodecs.BYTE_ARRAY.mapStream(Function.identity()));

        Registry.register(BuiltInRegistries.MENU, Exposure.resource(id), type);

        return () -> {
            return type;
        };
    }

    public static Supplier<RecipeType<?>> recipeType(String id, Supplier<RecipeType<?>> supplier) {
        RecipeType<?> obj = Registry.register(BuiltInRegistries.RECIPE_TYPE, Exposure.resource(id), supplier.get());
        return () -> obj;
    }

    public static Supplier<RecipeSerializer<?>> recipeSerializer(String id, Supplier<RecipeSerializer<?>> supplier) {
        RecipeSerializer<?> obj = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Exposure.resource(id), supplier.get());
        return () -> obj;
    }

    public static <T extends CriterionTrigger<?>> Supplier<T> criterionTrigger(String name, Supplier<T> supplier) {
        T obj = Registry.register(BuiltInRegistries.TRIGGER_TYPES, Exposure.resource(name), supplier.get());
        return () -> obj;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends MapCodec<?>> Supplier<T> entitySubPredicate(String name, Supplier<T> supplier) {
        T codec = supplier.get();
        Registry.register((Registry) BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, Exposure.resource(name), codec);
        return () -> codec;
    }

    public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>>
    Supplier<ArgumentTypeInfo<A, T>> commandArgumentType(String id, Class<A> infoClass, I argumentTypeInfo) {
        ArgumentTypeRegistry.registerArgumentType(Exposure.resource(id), infoClass, argumentTypeInfo);
        return () -> argumentTypeInfo;
    }

    public static <T extends FeatureConfiguration> Supplier<Feature<?>> worldGenFeature(String name, Supplier<Feature<T>> featureSupplier) {
        Feature<T> feature = Registry.register(BuiltInRegistries.FEATURE, name, featureSupplier.get());
        return () -> feature;
    }

    public static <T> DataComponentType<T> dataComponentType(String name, Consumer<DataComponentType.Builder<T>> builderConsumer) {
        var builder = DataComponentType.<T>builder();
        builderConsumer.accept(builder);
        var componentType = builder.build();
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Exposure.resource(name), componentType);
    }

    public static <T extends ParticleType<? extends ParticleOptions>> Supplier<T> particleType(String name, Supplier<T> supplier) {
        T particleType = Registry.register(BuiltInRegistries.PARTICLE_TYPE, name, supplier.get());
        return () -> particleType;
    }
}
