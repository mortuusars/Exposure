package io.github.mortuusars.exposure.world.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record CameraId(UUID uuid) {
    public static final Codec<CameraId> CODEC = UUIDUtil.CODEC.xmap(CameraId::new, CameraId::uuid);
    public static final StreamCodec<ByteBuf, CameraId> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(CameraId::new, CameraId::uuid);

    public static CameraId create() {
        return new CameraId(UUID.randomUUID());
    }

    public static CameraId ofStack(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_ID, new CameraId(Util.NIL_UUID));
    }

    public boolean matches(ItemStack stack) {
        return equals(stack.get(Exposure.DataComponents.CAMERA_ID));
    }

    @Override
    public String toString() {
        return uuid().toString();
    }
}
