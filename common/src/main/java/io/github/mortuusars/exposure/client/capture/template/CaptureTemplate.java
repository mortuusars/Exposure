package io.github.mortuusars.exposure.client.capture.template;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmProperties;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmStyle;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public interface CaptureTemplate {
    Task<?> createTask(CaptureParameters params);

    default Holder<ColorPalette> getColorPalette(CaptureParameters params) {
        return ColorPalettes.get(Minecrft.registryAccess(), params.filmProperties().colorPalette());
    }

    default Function<Image, Image> applyEffectsToImage(CaptureParameters params) {
        FilmProperties film = params.filmProperties();
        FilmStyle style = film.style();
        return ImageEffect.chain(
                ImageEffect.Crop.SQUARE_CENTER,
                ImageEffect.Crop.factor(params.cropFactor()),
                ImageEffect.Resize.to(film.size().orElse(Config.Server.DEFAULT_FRAME_SIZE.get())),
                ImageEffect.exposure(params.getShutterSpeed().getBrightness() * (style.sensitivity() + 1)),
                ImageEffect.contrast(style.contrast()),
                ImageEffect.levels(style.levels()),
                ImageEffect.hsb(style.hsb()),
                ImageEffect.noise(style.noise()),
                ImageEffect.optional(params.filmProperties().type() == ExposureType.BLACK_AND_WHITE,
                        params.singleChannel()
                                .map(ImageEffect::singleChannelBlackAndWhite)
                                .orElse(ImageEffect.BLACK_AND_WHITE)));
    }

    default Function<PalettedImage, ExposureData> convertToExposureData(Holder<ColorPalette> palette, ExposureData.Tag tag) {
        Identifier paletteId = palette.unwrapKey().orElseThrow().identifier();
        return image -> new ExposureData(image.width(), image.height(), image.pixels(), paletteId, tag);
    }

    default ExposureData.Tag createExposureTag(CaptureParameters params, boolean isLoaded) {
        return new ExposureData.Tag(params.filmProperties().type(), Minecrft.player().getScoreboardName(),
                UnixTimestamp.Seconds.now(), isLoaded, false);
    }

    default @NotNull Consumer<TranslatableError> printCasualErrorInChat() {
        return err -> {
            Minecrft.execute(() -> Minecrft.player().displayClientMessage(
                    err.casual().withStyle(ChatFormatting.RED), false));
            Exposure.LOGGER.error(err.technical().getString());
        };
    }
}
