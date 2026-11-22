package io.github.mortuusars.exposure.world.camera.frame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.UUID;

public final class Photographer {
    public static final Photographer EMPTY = new Photographer("", Util.NIL_UUID);

    public static final Codec<Photographer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("name", "").forGetter(Photographer::name),
                    UUIDUtil.LENIENT_CODEC.optionalFieldOf("uuid", Util.NIL_UUID).forGetter(Photographer::uuid))
            .apply(instance, Photographer::new));

    public static final StreamCodec<ByteBuf, Photographer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Photographer::name,
            UUIDUtil.STREAM_CODEC, Photographer::uuid,
            Photographer::new
    );

    private final String name;
    private final UUID uuid;

    private Photographer(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public Photographer(CameraHolder cameraHolder) {
        Entity owner = cameraHolder.getExposureAuthorEntity();
        this.name = owner instanceof Player ? owner.getScoreboardName() : EntityType.getKey(owner.getType()).toString();
        // UUID of non-player entities are not recorded because they are usually short-lived.
        this.uuid = owner instanceof Player ? owner.getUUID() : Util.NIL_UUID;
    }

    public boolean matches(Entity entity) {
        return uuid.equals(entity.getUUID());
    }

    public boolean isPlayer() {
        return !StringUtil.isBlank(name) && !uuid.equals(Util.NIL_UUID);
    }

    public boolean isNPC() {
        return !StringUtil.isBlank(name) && uuid.equals(Util.NIL_UUID);
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public String name() {
        return name;
    }

    public UUID uuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Photographer) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid);
    }

    @Override
    public String toString() {
        return "Photographer[" +
                "name=" + name + ", " +
                "uuid=" + uuid + ']';
    }

}
