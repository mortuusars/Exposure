package io.github.mortuusars.exposure.client.image.modifier;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.image.CensoredImage;
import io.github.mortuusars.exposure.client.image.CroppedImage;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ResizedImage;
import io.github.mortuusars.exposure.client.image.modifier.pixel.*;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.util.Rect2i;
import io.github.mortuusars.exposure.world.camera.film.properties.HSB;
import io.github.mortuusars.exposure.world.camera.film.properties.Levels;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface ImageEffect {
    /**
     * Unique identifier to differentiate between rendered images.<br>
     * It is used in {@link net.minecraft.resources.Identifier} so choose valid chars as invalid ones will be removed.<br>
     * To make it more readable, separate parts by dash: brightness-1.5
     * and join multiple identifiers with underscore: brightness-1.5_contrast-0.8
     */
    String getIdentifier();
    Image modify(Image image);

    // --

    ImageEffect EMPTY = new Instance("", Function.identity());

    ImageEffect CENSORED = new Instance("censored", CensoredImage::new);
    ImageEffect NEGATIVE = new NegativeEffect();
    ImageEffect NEGATIVE_FILM = new NegativeFilmEffect();
    ImageEffect AGED = new AgedHSBEffect(0xD9A863, 0.65f, 40, 255);
    ImageEffect BLACK_AND_WHITE = new BlackAndWhiteEffect(0.299f, 0.587f, 0.114f);

    static ImageEffect exposure(float exposure) {
        return exposure != 1f ? new ExposureEffect(exposure) : EMPTY;
    }

    static ImageEffect contrast(float contrast) {
        return contrast != 0f ? new ContrastEffect(contrast) : EMPTY;
    }

    static ImageEffect noise(float noise) {
        return noise != 0f ? new NoiseEffect(Mth.clamp(noise, 0f, 1f)) : EMPTY;
    }

    static ImageEffect levels(Levels levels) {
        return !levels.equals(Levels.EMPTY) ? new LevelsEffect(levels) : EMPTY;
    }

    static ImageEffect hsb(HSB hsb) {
        return hsb(hsb.hue(), hsb.saturation(), hsb.brightness());
    }

    static ImageEffect hsb(float hue, float saturation, float brightness) {
        return hue != 0 || saturation != 0 || brightness != 0 ? new HSBEffect(hue, saturation, brightness) : EMPTY;
    }

    static ImageEffect colorBalance(float r, float g, float b) {
        return new ColorBalanceEffect(r, g, b);
    }

    static ImageEffect singleChannelBlackAndWhite(ColorChannel colorChannel) {
        return switch (colorChannel) {
            case RED -> new BlackAndWhiteEffect(1f, 0.05f, 0.05f);
            case GREEN -> new BlackAndWhiteEffect(0.05f, 1f, 0.05f);
            case BLUE -> new BlackAndWhiteEffect(0.05f, 0.05f, 1f);
        };
    }

    interface Crop extends ImageEffect {
        ImageEffect SQUARE_CENTER = new Instance("crop-square", image -> {
            int smallerSide = Math.min(image.width(), image.height());
            int x = (image.width() - smallerSide) / 2;
            int y = (image.height() - smallerSide) / 2;
            return new CroppedImage(image, new Rect2i(x, y, smallerSide, smallerSide));
        });

        static ImageEffect factor(double factor) {
            if (factor == 1f) return EMPTY;
            double clampedFactor = Math.min(1.0, factor);
            return new Instance("crop-factor-" + String.format("%,.4f", clampedFactor), image -> {
                int newWidth = (int) (image.width() * clampedFactor);
                int newHeight = (int) (image.height() * clampedFactor);
                int x = (image.width() - newWidth) / 2;
                int y = (image.height() - newHeight) / 2;
                return new CroppedImage(image, new Rect2i(x, y, newWidth, newHeight));
            });
        }

        static ImageEffect factor(float factor) {
            return factor((double)factor);
        }
    }

    interface Resize extends ImageEffect {
        static ImageEffect to(int width, int height) {
            return new Instance("resized-%sx%s".formatted(width, height), image -> new ResizedImage(image, width, height));
        }

        static ImageEffect to(int size) {
            return to(size, size);
        }

        static ImageEffect multiplier(int multiplier) {
            Preconditions.checkArgument(multiplier > 0, "multiplier should be larger than 0.");
            return new Instance("resized-%sx".formatted(multiplier),
                    image -> new ResizedImage(image, image.width() * multiplier, image.height() * multiplier));
        }
    }

    // --

    static ImageEffect composite(ImageEffect... modifiers) {
        if (modifiers.length == 0) return EMPTY;
        return new Composite(modifiers);
    }

    static Function<Image, Image> chain(ImageEffect... modifiers) {
        return composite(modifiers)::modify;
    }

    static ImageEffect optional(boolean condition, ImageEffect modifier) {
        return condition ? modifier : EMPTY;
    }

    static ImageEffect optional(boolean condition, Supplier<ImageEffect> supplier) {
        return condition ? supplier.get() : EMPTY;
    }

    static <T> ImageEffect optional(Function<T, ImageEffect> modifier, Optional<T> optional) {
        return optional.map(modifier).orElse(ImageEffect.EMPTY);
    }

    record Instance(String identifier, Function<Image, Image> processingFunction) implements ImageEffect {
        @Override
        public Image modify(Image image) {
            return processingFunction.apply(image);
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }
    }

    record Composite(ImageEffect... modifiers) implements ImageEffect {
        @Override
        public Image modify(Image image) {
            for (ImageEffect modifier : modifiers) {
                image = modifier.modify(image);
            }
            return image;
        }

        @Override
        public String getIdentifier() {
            return Arrays.stream(modifiers)
                    .filter(filter -> !filter.equals(EMPTY))
                    .map(ImageEffect::getIdentifier)
                    .collect(Collectors.joining("_"));
        }
    }
}
