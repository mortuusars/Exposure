package io.github.mortuusars.exposure.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;

import java.util.function.Function;

public class Codecs {
    public static Codec<int[]> intArrayCodec(int minSize, int maxSize) {
        return new Codec<>() {
            @Override
            public <T> DataResult<T> encode(int[] input, DynamicOps<T> ops, T prefix) {
                if (input.length < minSize || input.length > maxSize)
                    return DataResult.error(() -> "Array size must be between " + minSize + " and " + maxSize + ", got: " + input.length);
                ListBuilder<T> builder = ops.listBuilder();
                for (int inp : input)
                    builder.add(ops.createInt(inp));
                return builder.build(prefix);
            }

            @Override
            public <T> DataResult<Pair<int[], T>> decode(DynamicOps<T> ops, T input) {
                return ops.getIntStream(input)
                        .flatMap(stream -> {
                            int[] array = stream.toArray();
                            if (array.length < minSize || array.length > maxSize)
                                return DataResult.error(() -> "Array size must be between " + minSize + " and " + maxSize + ", got: " + array.length);
                            return DataResult.success(Pair.of(array, input));
                        });
            }
        };
    }

    public static Codec<byte[]> byteArrayCodec(int minSize, int maxSize) {
        return new Codec<>() {
            @Override
            public <T> DataResult<T> encode(byte[] input, DynamicOps<T> ops, T prefix) {
                if (input.length < minSize || input.length > maxSize)
                    return DataResult.error(() -> "Array size must be between " + minSize + " and " + maxSize + ", got: " + input.length);
                ListBuilder<T> builder = ops.listBuilder();
                for (byte inp : input)
                    builder.add(ops.createByte(inp));
                return builder.build(prefix);
            }

            @Override
            public <T> DataResult<Pair<byte[], T>> decode(DynamicOps<T> ops, T input) {
                return ops.getByteBuffer(input)
                        .flatMap(stream -> {
                            byte[] array = stream.array();
                            if (array.length < minSize || array.length > maxSize)
                                return DataResult.error(() -> "Array size must be between " + minSize + " and " + maxSize + ", got: " + array.length);
                            return DataResult.success(Pair.of(array, input));
                        });
            }
        };
    }

    public static final Codec<Float> POSITIVE_FLOAT = Codec.FLOAT.validate(f -> f > 0
            ? DataResult.success(f)
            : DataResult.error(() -> "Value must be positive: " + f));

    public static Codec<Float> floatRange(float minInclusive, float maxInclusive) {
        return Codec.FLOAT.validate(f -> f.compareTo(minInclusive) >= 0 && f.compareTo(maxInclusive) <= 0
                        ? DataResult.success(f)
                        : DataResult.error(() -> "Value must be between %s and %s: %s".formatted(minInclusive, maxInclusive, f)));
    }
}
