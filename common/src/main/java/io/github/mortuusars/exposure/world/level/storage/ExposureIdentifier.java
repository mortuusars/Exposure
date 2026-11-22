package io.github.mortuusars.exposure.world.level.storage;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExposureIdentifier {
    public static final ExposureIdentifier EMPTY = new ExposureIdentifier("", null);

    public static final Codec<ExposureIdentifier> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<T> encode(ExposureIdentifier input, DynamicOps<T> ops, T prefix) {
            if (input.isEmpty()) return DataResult.success(ops.createString(""));
            return input.map(id -> SIMPLE_ID_CODEC, texture -> FULL_CODEC).encode(input, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<ExposureIdentifier, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<ExposureIdentifier, T>> simpleResult = SIMPLE_ID_CODEC.decode(ops, input);
            if (simpleResult.error().isPresent()) {
                return FULL_CODEC.decode(ops, input);
            }
            return simpleResult;
        }
    };

    public static final Codec<ExposureIdentifier> SIMPLE_ID_CODEC = Codec.STRING.flatComapMap(
            ExposureIdentifier::id,
            identifier -> identifier.id != null
                    ? DataResult.success(identifier.id)
                    : DataResult.error(() -> "Cannot serialize to string: id is null."));

    public static final Codec<ExposureIdentifier> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("id").forGetter(ei -> Optional.ofNullable(ei.id)),
            Identifier.CODEC.optionalFieldOf("texture").forGetter(ei -> Optional.ofNullable(ei.texture))
    ).apply(instance, (idOpt, textureOpt) -> new ExposureIdentifier(
            idOpt.orElse(null),
            textureOpt.orElse(null)
    )));

    public static final StreamCodec<FriendlyByteBuf, ExposureIdentifier> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ExposureIdentifier decode(FriendlyByteBuf buffer) {
            boolean isTexture = buffer.readBoolean();
            return isTexture
                    ? ExposureIdentifier.texture(buffer.readIdentifier())
                    : ExposureIdentifier.id(buffer.readUtf());
        }

        @Override
        public void encode(FriendlyByteBuf buffer, ExposureIdentifier instance) {
            buffer.writeBoolean(instance.isTexture());
            instance.ifId(buffer::writeUtf)
                    .ifTexture(buffer::writeIdentifier);
        }
    };

    @Nullable
    private final String id;
    @Nullable
    private final Identifier texture;

    private ExposureIdentifier(@Nullable String id, @Nullable Identifier texture) {
        Preconditions.checkArgument(id == null || texture == null,
                "Cannot have both id and texture defined at once. Only one of them should be present.");
        this.id = id;
        this.texture = texture;
    }

    public static ExposureIdentifier id(@NotNull String id) {
        return new ExposureIdentifier(id, null);
    }

    public static ExposureIdentifier texture(@NotNull Identifier texture) {
        return new ExposureIdentifier(null, texture);
    }

    public boolean isEmpty() {
        return this == EMPTY || (id != null && id.isEmpty());
    }

    public @Nullable String id() {
        return id;
    }

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public @Nullable Identifier texture() {
        return texture;
    }

    public Optional<Identifier> getTexture() {
        return Optional.ofNullable(texture);
    }

    @Contract()
    public boolean isId() {
        return id != null;
    }

    public boolean isTexture() {
        return texture != null;
    }

    public <T> T map(final Function<String, T> ifId, final Function<Identifier, T> ifTexture) {
        return isId() ? ifId.apply(id) : ifTexture.apply(texture());
    }

    public ExposureIdentifier ifId(Consumer<String> idConsumer) {
        if (isId()) {
            idConsumer.accept(id);
        }
        return this;
    }

    public <T> Optional<T> mapId(Function<String, T> mappingFunc) {
        return isId() ? Optional.of(mappingFunc.apply(id)) : Optional.empty();
    }

    public ExposureIdentifier ifTexture(Consumer<Identifier> textureConsumer) {
        if (isTexture()) {
            textureConsumer.accept(texture());
        }
        return this;
    }

    public <T> Optional<T> mapTexture(Function<Identifier, T> mappingFunc) {
        return isTexture() ? Optional.of(mappingFunc.apply(texture)) : Optional.empty();
    }

    public String toValueString() {
        return map(Function.identity(), Identifier::toString);
    }

    @Override
    public String toString() {
        return isEmpty() ? "" : map(id -> "Id: " + id, texture -> "Texture: " + texture);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExposureIdentifier that = (ExposureIdentifier) o;
        return Objects.equals(id, that.id) && Objects.equals(texture, that.texture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, texture);
    }

    // --

    private static int sameTickCounter = 0;
    private static long lastTick = -1;

    /**
     * Creates ids using entity name and current gameTime.<br>
     * Multiple ids created on the same tick will be differentiated by a count suffix.
     */
    public static String createId(Entity entity, String... middleParts) {
        if (entity.level().getGameTime() != lastTick) {
            lastTick = entity.level().getGameTime();
            sameTickCounter = 0;
        }

        List<String> parts = new ArrayList<>();
        parts.add(entity.getName().getString());
        parts.addAll(Arrays.asList(middleParts));
        parts.add(Long.toString(entity.level().getGameTime()));

        // Keep ids created in the same tick unique:
        if (sameTickCounter > 0) {
            parts.add(Integer.toString(sameTickCounter));
        }

        sameTickCounter++;

        return createId(parts.toArray(String[]::new));
    }

    public static String createId(String... parts) {
        Preconditions.checkArgument(parts.length > 0, "Cannot compose ID with 0 parts.");
        List<String> sanitizedParts = Arrays.stream(parts)
                .filter(s -> !StringUtil.isNullOrEmpty(s))
                .map(s -> s.replaceAll("[^A-Za-z0-9-]", "-"))
                .toList();
        return String.join("_", sanitizedParts);
    }
}
