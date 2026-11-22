package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class PathCaptureTask extends Task<Result<Image>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final TranslatableError ERROR_PATH_EMPTY = new TranslatableError("error.exposure.capture.path.path_empty", "ERR_PATH_EMPTY");
    public static final TranslatableError ERROR_PATH_INVALID = new TranslatableError("error.exposure.capture.path.path_invalid", "ERR_PATH_INVALID");

    protected final String path;

    public PathCaptureTask(String path) {
        this.path = path;
    }

    @Override
    public CompletableFuture<Result<Image>> execute() {
        if (StringUtil.isBlank(path)) {
            return CompletableFuture.completedFuture(Result.error(ERROR_PATH_EMPTY));
        }

        if (hasHttpPrefix(path)) {
            try {
                URI uri = new URI(path);
                URL url = uri.toURL();
                return new UrlCaptureTask(url).execute();
            } catch (Exception e) {
                LOGGER.error("Path '{}' has http/s prefix, but is not valid. Error: {}", path, e.getMessage());
                return CompletableFuture.completedFuture(Result.error(ERROR_PATH_INVALID));
            }
        }

        if (looksLikeURL(path)) {
            LOGGER.error("Path '{}' looks like a URL, but does not have http/s prefix.", path);
            return CompletableFuture.completedFuture(Result.error(UrlCaptureTask.ERROR_NO_HTTP_PREFIX));
        }

        if (isValidFilePath(path)) {
            return new FileCaptureTask(new File(path)).execute();
        }

        LOGGER.error("Path '{}' cannot be handled properly.", path);
        return CompletableFuture.completedFuture(Result.error(ERROR_PATH_INVALID));
    }

    private static boolean hasHttpPrefix(String path) {
        return path.startsWith("https://") || path.startsWith("http://");
    }

    private static boolean looksLikeURL(String path) {
        if (hasHttpPrefix(path)) {
            return true;
        }

        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == ':' || c == '/' || c == '\\') return false;
            if (c == '.') return true;
        }

        return false;
    }

    private static boolean isValidFilePath(String filePath) {
        try {
            Paths.get(filePath);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }
}
