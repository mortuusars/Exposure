package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.WrappedBufferedImage;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class UrlCaptureTask extends Task<Result<Image>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final TranslatableError ERROR_CANNOT_READ = new TranslatableError("error.exposure.capture.url.cannot_read", "ERR_CANNOT_READ");
    public static final TranslatableError ERROR_NO_HTTP_PREFIX = new TranslatableError("error.exposure.capture.url.no_http_prefix", "ERR_NO_HTTP_PREFIX");
    public static final TranslatableError ERROR_INVALID_URL = new TranslatableError("error.exposure.capture.url.invalid_url", "ERR_INVALID_URL");
    public static final TranslatableError ERROR_TIMED_OUT = new TranslatableError("error.exposure.capture.url.timed_out", "ERR_TIMED_OUT");

    protected final URL url;

    protected final CompletableFuture<Result<Image>> future = new CompletableFuture<>();

    public UrlCaptureTask(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public CompletableFuture<Result<Image>> execute() {
        return future.completeAsync(() -> {
            LOGGER.info("Attempting to load image from URL: '{}'", url.toString());

            try {
                @Nullable BufferedImage image = ImageIO.read(url);

                if (image == null) {
                    LOGGER.error("Cannot load image from URL '{}'", url);
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