package io.github.mortuusars.exposure.client.capture;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.capture.action.CompositeAction;
import io.github.mortuusars.exposure.client.capture.task.*;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import org.slf4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Capture<T> extends Task<Result<T>> {
    public static final TranslatableError ERROR_TIMED_OUT = new TranslatableError("error.exposure.capture.timed_out", "ERR_CAPTURE_TIMED_OUT");
    public static final TranslatableError ERROR_FAILED_GENERIC = new TranslatableError("error.exposure.capture.failed", "ERR_CAPTURE_FAILED");

    public static final int TIMEOUT_MS = 12_000; // 12 seconds
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Task<Result<T>> capturingTask;
    protected final CaptureAction component;
    protected final CaptureTimer timer;
    protected final CompletableFuture<Result<T>> completableFuture;

    public Capture(Task<Result<T>> capturingTask, CaptureAction component) {
        this.capturingTask = capturingTask;
        this.component = component;
        this.timer = new CaptureTimer(component.requiredDelayTicks())
                .whenStarted(this.component::initialize)
                .onGameTick(this.component::delayTick)
                .whenEnded(() -> {
                    this.component.beforeCapture();
                    capture();
                });
        this.completableFuture = new CompletableFuture<>();
    }

    public CompletableFuture<Result<T>> execute() {
        if (!timer.isRunning()) {
            timer.start();
            setStarted();
        }

        return completableFuture;
    }

    public void tick() {
        capturingTask.tick();
        timer.tick();
    }

    private void capture() {
        capturingTask.execute()
                .completeOnTimeout(Result.error(ERROR_TIMED_OUT), TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    LOGGER.error("Capturing failed: {}", throwable.toString());
                    return Result.error(ERROR_FAILED_GENERIC);
                })
                .thenApply(result -> {
                    if (result.isSuccessful()) {
                        component.onSuccess();
                    } else {
                        component.onFailure(result.getError());
                    }
                    component.afterCapture();
                    return result;
                })
                .thenAccept(result -> {
                    setDone();
                    completableFuture.complete(result);
                });
    }

    public Task<T> handleErrorAndGetResult() {
        return handleErrorAndGetResult(err -> {});
    }

    public Task<T> handleErrorAndGetResult(Consumer<TranslatableError> errorConsumer) {
        return onError(errorConsumer).then(Result::unwrap);
    }

    public Task<T> logErrorAndGetResult(Logger logger) {
        return onError(error -> logger.error(error.technical().getString())).then(Result::unwrap);
    }

    public static <T> Capture<T> of(Task<Result<T>> capturingTask) {
        return new Capture<>(capturingTask, CaptureAction.EMPTY);
    }

    public static <T> Capture<T> of(Task<Result<T>> capturingTask, CaptureAction action) {
        return new Capture<>(capturingTask, action);
    }

    public static <T> Capture<T> of(Task<Result<T>> capturingTask, CaptureAction... actions) {
        return new Capture<>(capturingTask, new CompositeAction(actions));
    }

    public static Task<Result<Image>> screenshot() {
        return ExposureClient.shouldUseDirectCapture()
                ? new DirectScreenshotCaptureTask()
                : new BackgroundScreenshotCaptureTask();
    }

    public static Task<Result<Image>> fromFile(File file) {
        return new FileCaptureTask(file);
    }

    public static Task<Result<Image>> fromUrl(URL url) {
        return new UrlCaptureTask(url);
    }

    public static Task<Result<Image>> path(String path) {
        return new PathCaptureTask(path);
    }
}
