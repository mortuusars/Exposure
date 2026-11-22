
package io.github.mortuusars.exposure.client.capture.task;

import com.google.common.io.Files;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.capture.task.file.ImageFileLoader;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.util.TranslatableError;
import net.minecraft.SharedConstants;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FileCaptureTask extends Task<Result<Image>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final TranslatableError ERROR_PATH_EMPTY = new TranslatableError("error.exposure.capture.file.path_empty", "ERR_PATH_EMPTY");
    public static final TranslatableError ERROR_PATH_INVALID = new TranslatableError("error.exposure.capture.file.path_invalid", "ERR_PATH_INVALID");
    public static final TranslatableError ERROR_NO_EXTENSION = new TranslatableError("error.exposure.capture.file.no_extension", "ERR_NO_EXTENSION");
    public static final TranslatableError ERROR_PATH_IS_DIRECTORY = new TranslatableError("error.exposure.capture.file.path_is_directory", "ERR_PATH_IS_DIRECTORY");
    public static final TranslatableError ERROR_FILE_DOES_NOT_EXIST = new TranslatableError("error.exposure.capture.file.file_does_not_exist", "ERR_FILE_DOES_NOT_EXIST");
    public static final TranslatableError ERROR_CANNOT_READ = new TranslatableError("error.exposure.capture.file.cannot_read", "ERR_CANNOT_READ");
    public static final TranslatableError ERROR_NOT_SUPPORTED = new TranslatableError("error.exposure.capture.file.not_supported", "ERR_NOT_SUPPORTED");
    public static final TranslatableError ERROR_TIMED_OUT = new TranslatableError("error.exposure.capture.file.timed_out", "ERR_TIMED_OUT");

    protected final File file;

    protected final CompletableFuture<Result<Image>> future = new CompletableFuture<>();

    public FileCaptureTask(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public CompletableFuture<Result<Image>> execute() {
        return future.completeAsync(() -> {
            LOGGER.info("Attempting to load image from file: '{}'", file.toString());

            Result<File> result = findFileWithExtension(file);

            if (result.isError()) {
                return result.remapError();
            }

            result = validateFilepath(result.getValue());

            if (result.isError()) {
                return result.remapError();
            }

            File file = result.getValue();

            LOGGER.info("Reading image from file: '{}'", file);

            return ImageFileLoader.chooseFitting(file).load(file);
        }).completeOnTimeout(Result.error(ERROR_TIMED_OUT),
                Config.Server.PROJECT_TIMEOUT_TICKS.get() * SharedConstants.MILLIS_PER_TICK, TimeUnit.MILLISECONDS);
    }

    private static Result<File> validateFilepath(File file) {
        String filepath = file.getPath();

        if (StringUtil.isNullOrEmpty(filepath)) {
            return Result.error(ERROR_PATH_EMPTY);
        }

        if (file.isDirectory()) {
            return Result.error(ERROR_PATH_IS_DIRECTORY);
        }

        String extension = Files.getFileExtension(filepath);
        if (StringUtil.isNullOrEmpty(extension)) {
            return Result.error(ERROR_NO_EXTENSION);
        }

        if (!file.exists()) {
            return Result.error(ERROR_FILE_DOES_NOT_EXIST);
        }

        return Result.success(file);
    }

    /**
     * If provided filepath is missing an extension - searches for first file
     * in parent directory that matches the name of given file.
     *
     * @return File with extension or error.
     */
    private static Result<File> findFileWithExtension(File file) {
        String extension = Files.getFileExtension(file.getAbsolutePath());
        if (!StringUtil.isNullOrEmpty(extension)) {
            return Result.success(file);
        }

        @Nullable File parentFile = file.getParentFile();
        if (parentFile == null) {
            return Result.error(ERROR_PATH_INVALID);
        }

        File[] files = parentFile.listFiles();
        if (files == null) {
            return Result.error(ERROR_CANNOT_READ);
        }

        String name = file.getName();
        for (File fileInDirectory : files) {
            if (fileInDirectory.isDirectory()) {
                continue;
            }

            String fileName = Files.getNameWithoutExtension(fileInDirectory.getName());
            if (fileName.equals(name)) {
                return Result.success(fileInDirectory);
            }
        }

        return Result.error(ERROR_FILE_DOES_NOT_EXIST);
    }
}
