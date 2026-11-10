package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.WrappedBufferedImage;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.IDN;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class UrlCaptureTask extends Task<Result<Image>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final TranslatableError ERROR_CANNOT_READ = new TranslatableError("error.exposure.capture.url.cannot_read", "ERR_CANNOT_READ");
    public static final TranslatableError ERROR_NO_HTTP_PREFIX = new TranslatableError("error.exposure.capture.url.no_http_prefix", "ERR_NO_HTTP_PREFIX");
    public static final TranslatableError ERROR_INVALID_URL = new TranslatableError("error.exposure.capture.url.invalid_url", "ERR_INVALID_URL");
    public static final TranslatableError ERROR_TIMED_OUT = new TranslatableError("error.exposure.capture.url.timed_out", "ERR_TIMED_OUT");

    protected final URI uri;

    protected final CompletableFuture<Result<Image>> future = new CompletableFuture<>();

    public UrlCaptureTask(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public boolean isUrlValid() {
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            return false;
        }

        String host = uri.getHost();
        if (host == null) {
            return false;
        }

        if (Minecrft.get().isSingleplayer()) {
            return true;
        }

        host = IDN.toASCII(host.toLowerCase(Locale.ROOT));

        return switch (Config.Client.URL_LOADING.get()) {
            case ALL -> true;
            case ONLY_ALLOWED_DOMAINS -> {
                if (!Config.Client.URL_LOADING_ALLOWED_DOMAINS.get().contains(host)
                      && Config.Client.URL_LOADING_ALLOWED_SUBDOMAINS.get().stream().noneMatch(host::endsWith)) {
                    LOGGER.error("Domain is not allowed for image loading. URL: {}", uri);
                    Minecrft.playerOrThrow().displayClientMessage(Component.literal("Domain is not allowed for image projecting.")
                          .withStyle(ChatFormatting.RED), false);
                    yield false;
                }

                try {
                    InetAddress[] addresses = InetAddress.getAllByName(host);

                    for (InetAddress addr : addresses) {
                        if (addr.isAnyLocalAddress()
                              || addr.isLoopbackAddress()
                              || addr.isLinkLocalAddress()
                              || addr.isSiteLocalAddress()
                              || addr.isMulticastAddress()) {
                            LOGGER.error("Domain is pointing to local address. URL: {}", uri);
                            yield false;
                        }
                    }
                } catch (UnknownHostException e) {
                    LOGGER.error("Domain is unknown. URL: {}", uri);
                    yield false;
                }

                yield true;
            }
            case NONE -> {
                LOGGER.error("URL image loading is disabled. URL: {}", uri);
                yield false;
            }
        };
    }

    @Override
    public CompletableFuture<Result<Image>> execute() {
        return future.completeAsync(() -> {
            if (!isUrlValid()) {
                LOGGER.error("URL '{}' is not valid. Image would not be loaded.", uri);
                return Result.error(ERROR_INVALID_URL);
            }

            LOGGER.info("Attempting to load image from URL: '{}'", uri.toString());

            try {
                @Nullable BufferedImage image = ImageIO.read(uri.toURL());

                if (image == null) {
                    LOGGER.error("Cannot load image from URL '{}'", uri);
                    return Result.error(ERROR_CANNOT_READ);
                }

                if (image.getWidth() > 10_000 || image.getHeight() > 10_000) {
                    LOGGER.error("Cannot load image from URL '{}': image is too large.", uri);
                    return Result.error(ERROR_CANNOT_READ);
                }

                return Result.success(new WrappedBufferedImage(image));
            } catch (Exception e) {
                LOGGER.error("Cannot load image from URL: ", e);
                return Result.error(ERROR_CANNOT_READ);
            }
        }).completeOnTimeout(Result.error(ERROR_TIMED_OUT),
                Config.Server.PROJECT_TIMEOUT_TICKS.get() * SharedConstants.MILLIS_PER_TICK, TimeUnit.MILLISECONDS);
    }
}