package io.github.mortuusars.exposure.world.item.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ActiveCameraSetSettingC2SP;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.sound.Sound;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record CameraSetting<T>(DataComponentType<T> component, T defaultValue, Optional<SoundEffect> sound) {
    public static final Codec<CameraSetting<?>> CODEC = Identifier.CODEC.xmap(CameraSettings::byId, CameraSettings::idOf);
    public static final StreamCodec<ByteBuf, CameraSetting<?>> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, CameraSettings::idOf,
            CameraSettings::byId
    );

    public CameraSetting(DataComponentType<T> component, T defaultValue, SoundEffect sound) {
        this(component, defaultValue, Optional.ofNullable(sound));
    }

    public CameraSetting(DataComponentType<T> component, T defaultValue) {
        this(component, defaultValue, Optional.empty());
    }

    // --

    public @Nullable T get(ItemStack stack) {
        return stack.get(component);
    }

    public Optional<T> getOptional(ItemStack stack) {
        return Optional.ofNullable(get(stack));
    }

    public T getOrDefault(ItemStack stack) {
        return stack.getOrDefault(component, defaultValue);
    }

    public T getOrElse(ItemStack stack, T defaultValue) {
        return stack.getOrDefault(component, defaultValue);
    }

    public @Nullable T get(Camera camera) {
        return camera.getItemStack().get(component);
    }

    public Optional<T> getOptional(Camera camera) {
        return Optional.ofNullable(get(camera));
    }

    public T getOrDefault(Camera camera) {
        return camera.getItemStack().getOrDefault(component, defaultValue);
    }

    public T getOrElse(Camera camera, T defaultValue) {
        return camera.getItemStack().getOrDefault(component, defaultValue);
    }

    // --

    public boolean set(ItemStack stack, T value) {
        if (stack.isEmpty() || getOrDefault(stack).equals(value)) return false;

        if (value instanceof Boolean bool && !bool) {
            stack.remove(component);
        } else {
            stack.set(component, value);
        }
        return true;
    }

    public boolean set(CameraHolder holder, ItemStack stack, T value) {
        if (stack.getItem() instanceof CameraItem cameraItem && set(stack, value)) {
            cameraItem.actionPerformed(stack, holder);
            sound.ifPresent(sound ->
                    Sound.playSided(holder.asHolderEntity(), sound.sound().get(), SoundSource.PLAYERS,
                            sound.volume(), sound.pitch(), sound.pitchVariability()));
            return true;
        }
        return false;
    }

    public boolean set(Camera camera, T value) {
        return camera.map((item, stack) -> set(camera.getHolder(), stack, value)).orElse(false);
    }

    public boolean setAndSync(Camera camera, T value) {
        return camera.map((item, stack) -> {
            if (set(camera.getHolder(), stack, value)) {
                byte[] bytes = encodeValue(camera.getHolder().asHolderEntity().registryAccess(), value);
                Packets.sendToServer(new ActiveCameraSetSettingC2SP(this, bytes));
                return true;
            }
            return false;
        }).orElse(false);
    }

    public boolean decodeAndSet(ItemStack stack, RegistryAccess registryAccess, byte[] bytes) {
        T value = decodeValue(registryAccess, bytes);
        return set(stack, value);
    }

    public boolean decodeAndSet(CameraHolder holder, ItemStack stack, RegistryAccess registryAccess, byte[] bytes) {
        T value = decodeValue(registryAccess, bytes);
        return set(holder, stack, value);
    }

    // --

    public byte[] encodeValue(RegistryAccess registryAccess, T value) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        try {
            component.streamCodec().encode(buffer, value);
            return buffer.array().clone();
        } finally {
            buffer.release();
        }
    }

    public T decodeValue(RegistryAccess registryAccess, byte[] bytes) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        try {
            buffer.writeBytes(bytes);
            return component.streamCodec().decode(buffer);
        } finally {
            buffer.release();
        }
    }
}
