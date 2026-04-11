package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.util.ExtraData;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * We cannot use {@link net.minecraft.advancements.criterion.NbtPredicate} as it checks against CompoundTag.class.
 * This is basically a copy of its functionality to make ExtraData work.
 */
public record ExtraDataPredicate(ExtraData data) {
    public static final Codec<ExtraDataPredicate> CODEC = TagParser.LENIENT_CODEC.xmap(
            tag -> new ExtraDataPredicate(new ExtraData(tag)),
            predicate -> predicate.data);
    public static final StreamCodec<ByteBuf, ExtraDataPredicate> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(
            tag -> new ExtraDataPredicate(new ExtraData(tag)),
            predicate -> predicate.data);

    public boolean matches(@Nullable Tag tag) {
        return tag != null && compareNbt(tag);
    }

    private boolean compareNbt(@Nullable Tag other) {
        if (data == other) {
            return true;
        } else if (data == null) {
            return true;
        } else if (other == null) {
            return false;
        } else if (data instanceof CompoundTag compoundTag) {
            CompoundTag compoundTag2 = (CompoundTag)other;
            if (compoundTag2.size() < compoundTag.size()) {
                return false;
            } else {
                for (String string : compoundTag.keySet()) {
                    Tag tag2 = compoundTag.get(string);
                    if (!NbtUtils.compareNbt(tag2, compoundTag2.get(string), true)) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return data.equals(other);
        }
    }
}
