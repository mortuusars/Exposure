package io.github.mortuusars.exposure.world.camera.capture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.Codecs;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmProperties;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public record CaptureParameters(String exposureId,
                                Optional<CameraId> cameraId,
                                Optional<Integer> cameraHolderId,
                                Optional<Float> fov,
                                float cropFactor,
                                Optional<Identifier> filter,
                                Optional<Projection> projection,
                                Optional<ColorChannel> singleChannel,
                                FilmProperties filmProperties,
                                ExtraData extraData) {

    public static final ExtraData.Type<ShutterSpeed> SHUTTER_SPEED =
            ExtraData.Type.stringRepresentable("shutter_speed", ShutterSpeed::new);
    public static final ExtraData.Type<Boolean> FLASH = ExtraData.Type.bool("flash");
    public static final ExtraData.Type<Integer> LIGHT_LEVEL = ExtraData.Type.intVal("light_level");

    // --

    public static final Codec<CaptureParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(CaptureParameters::exposureId),
            CameraId.CODEC.optionalFieldOf("camera_id").forGetter(CaptureParameters::cameraId),
            Codec.INT.optionalFieldOf("camera_holder_id").forGetter(CaptureParameters::cameraHolderId),
            Codecs.POSITIVE_FLOAT.optionalFieldOf("fov").forGetter(CaptureParameters::fov),
            Codecs.floatRange(0.001f, 1f).optionalFieldOf("crop_factor", 1f).forGetter(CaptureParameters::cropFactor),
            Identifier.CODEC.optionalFieldOf("filter").forGetter(CaptureParameters::filter),
            Projection.CODEC.optionalFieldOf("projection").forGetter(CaptureParameters::projection),
            ColorChannel.CODEC.optionalFieldOf("single_channel").forGetter(CaptureParameters::singleChannel),
            FilmProperties.CODEC.optionalFieldOf("film", FilmProperties.EMPTY).forGetter(CaptureParameters::filmProperties),
            ExtraData.CODEC.optionalFieldOf("extra_data", ExtraData.EMPTY).forGetter(CaptureParameters::extraData)
    ).apply(instance, CaptureParameters::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CaptureParameters> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull CaptureParameters decode(RegistryFriendlyByteBuf buffer) {
            return new CaptureParameters(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.optional(CameraId.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).decode(buffer),
                    ByteBufCodecs.optional(ByteBufCodecs.FLOAT).decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer),
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(Projection.STREAM_CODEC).decode(buffer),
                    ByteBufCodecs.optional(ColorChannel.STREAM_CODEC).decode(buffer),
                    FilmProperties.STREAM_CODEC.decode(buffer),
                    ExtraData.STREAM_CODEC.decode(buffer));
        }

        public void encode(RegistryFriendlyByteBuf buffer, CaptureParameters data) {
            ByteBufCodecs.STRING_UTF8.encode(buffer, data.exposureId());
            ByteBufCodecs.optional(CameraId.STREAM_CODEC).encode(buffer, data.cameraId());
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).encode(buffer, data.cameraHolderId());
            ByteBufCodecs.optional(ByteBufCodecs.FLOAT).encode(buffer, data.fov());
            ByteBufCodecs.FLOAT.encode(buffer, data.cropFactor());
            ByteBufCodecs.optional(Identifier.STREAM_CODEC).encode(buffer, data.filter());
            ByteBufCodecs.optional(Projection.STREAM_CODEC).encode(buffer, data.projection());
            ByteBufCodecs.optional(ColorChannel.STREAM_CODEC).encode(buffer, data.singleChannel());
            FilmProperties.STREAM_CODEC.encode(buffer, data.filmProperties());
            ExtraData.STREAM_CODEC.encode(buffer, data.extraData());
        }
    };

    public ShutterSpeed getShutterSpeed() {
        return extraData.get(SHUTTER_SPEED).orElse(ShutterSpeed.DEFAULT);
    }

    public boolean getFlash() {
        return extraData.get(FLASH).orElse(false);
    }

    public Optional<Integer> getLightLevel() {
        return extraData.get(LIGHT_LEVEL);
    }

    public Builder mutable() {
        return new Builder(this);
    }

    public static final class Builder {
        private final String exposureId;
        private @Nullable CameraId cameraId;
        private @Nullable Integer cameraHolderEntityID;
        private @Nullable Float fov;
        private float cropFactor = 1f;
        private @Nullable Identifier filter = null;
        private @Nullable Projection projection;
        private @Nullable ColorChannel chromaticChannel;
        private FilmProperties filmProperties = FilmProperties.EMPTY;
        private final ExtraData extraData = new ExtraData();

        public Builder(String exposureId) {
            this.exposureId = exposureId;
        }

        public Builder(CaptureParameters params) {
            this.exposureId = params.exposureId();
            this.cameraId = params.cameraId().orElse(null);
            this.cameraHolderEntityID = params.cameraHolderId().orElse(null);
            this.fov = params.fov.orElse(null);
            this.cropFactor = params.cropFactor();
            this.filter = params.filter().orElse(null);
            this.projection = params.projection().orElse(null);
            this.chromaticChannel = params.singleChannel().orElse(null);
            this.filmProperties = params.filmProperties();
            extraData.merge(params.extraData());
        }

        public Builder setCameraID(@Nullable CameraId cameraId) {
            this.cameraId = cameraId;
            return this;
        }

        public Builder setCameraHolder(@Nullable CameraHolder holder) {
            if (holder == null) cameraHolderEntityID = null;
            else cameraHolderEntityID = holder.asHolderEntity().getId();
            return this;
        }

        public Builder setFilter(@Nullable Identifier filter) {
            this.filter = filter;
            return this;
        }

        public Builder setFov(@Nullable Float fov) {
            this.fov = fov;
            return this;
        }

        public Builder setCropFactor(float cropFactor) {
            this.cropFactor = cropFactor;
            return this;
        }

        public Builder setProjectionInfo(@Nullable Projection projection) {
            this.projection = projection;
            return this;
        }

        public Builder setProjection(Optional<Projection> projection) {
            this.projection = projection.orElse(null);
            return this;
        }

        public Builder setChromaticChannel(@Nullable ColorChannel chromaticChannel) {
            this.chromaticChannel = chromaticChannel;
            return this;
        }

        public Builder setChromaticChannel(Optional<ColorChannel> chromaticChannel) {
            this.chromaticChannel = chromaticChannel.orElse(null);
            return this;
        }

        public Builder setFilmProperties(FilmProperties filmProperties) {
            this.filmProperties = filmProperties;
            return this;
        }

        public Builder extraData(Consumer<ExtraData> extraDataUpdater) {
            extraDataUpdater.accept(extraData);
            return this;
        }

        public <T> Builder extraData(ExtraData.Type<T> type, T value) {
            extraData.put(type, value);
            return this;
        }

        public CaptureParameters build() {
            return new CaptureParameters(exposureId,
                    Optional.ofNullable(this.cameraId),
                    Optional.ofNullable(this.cameraHolderEntityID),
                    Optional.ofNullable(this.fov),
                    this.cropFactor,
                    Optional.ofNullable(this.filter),
                    Optional.ofNullable(this.projection),
                    Optional.ofNullable(this.chromaticChannel),
                    this.filmProperties,
                    this.extraData);
        }
    }
}
