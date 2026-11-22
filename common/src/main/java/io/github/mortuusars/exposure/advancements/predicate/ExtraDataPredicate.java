package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.util.ExtraData;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public record ExtraDataPredicate(ExtraData data) {
    public static final Codec<ExtraDataPredicate> CODEC = TagParser.LENIENT_CODEC.xmap(
            tag -> new ExtraDataPredicate(new ExtraData(tag)), predicate -> predicate.data.toTag());
    public static final StreamCodec<ByteBuf, ExtraDataPredicate> STREAM_CODEC = ExtraData.STREAM_CODEC.map(
            ExtraDataPredicate::new, ExtraDataPredicate::data);

    public boolean matches(@Nullable ExtraData other) {
        return other != null && (data == null || NbtUtils.compareNbt(data.toTag(), other.toTag(), true));
    }
}
