package io.github.mortuusars.exposure.client.task;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.export.ImageExporter;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.mortaar.client.Minecrft;
import io.github.mortuusars.exposure.data.export.ExportLook;
import io.github.mortuusars.exposure.data.export.ExportSize;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.world.level.storage.RequestedPalettedExposure;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ExportExposuresTask extends Task<Result<Boolean>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    private static ExportExposuresTask currentTask;

    protected final List<String> ids;
    protected final ExportSize size;
    protected final ExportLook look;

    @Nullable
    protected CompletableFuture<Result<Boolean>> future;
    protected boolean stop;

    public ExportExposuresTask(List<String> ids, ExportSize size, ExportLook look) {
        this.ids = ids;
        this.size = size;
        this.look = look;
    }

    public static boolean isRunning() {
        return currentTask != null;
    }

    public static boolean start(List<String> ids, ExportSize size, ExportLook look) {
        if (ExportExposuresTask.isRunning()) {
            Minecrft.playerOrThrow().displayClientMessage(Component.translatable("task.exposure.export.already_running"), false);
            return false;
        }
        ExposureClient.cycles().addParallelTask(new ExportExposuresTask(ids, size, look));
        return true;
    }

    public static boolean stopCurrentTask() {
        if (currentTask != null) {
            currentTask.stop = true;
            return true;
        }
        return false;
    }

    @Override
    public CompletableFuture<Result<Boolean>> execute() {
        currentTask = this;
        if (future == null) {
            this.future = CompletableFuture.supplyAsync(() -> {
                try {
                    return export();
                } finally {
                    currentTask = null;
                }
            });
            setStarted();
        }
        return future;
    }

    protected Result<Boolean> export() {
        int total = ids.size();
        AtomicInteger exported = new AtomicInteger();

        print(Component.translatable("task.exposure.export.started", total));

        for (String id : ids) {
            if (stop) {
                print(Component.translatable("task.exposure.export.stopped"));
                return Result.success(false);
            }

            @Nullable ExposureData exposure = null;

            for (int attempt = 0; attempt < 50; attempt++) {
                updateStatus(Component.translatable("task.exposure.export.status.requesting", id, exported, total));

                RequestedPalettedExposure request = ExposureClient.exposureStore().getOrRequest(id);
                if (request.getData().isPresent()) {
                    exposure = request.getData().get();
                    break;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }

            if (exposure == null) {
                print(Component.translatable("task.exposure.export.error.not_received", id));
                continue;
            }

            updateStatus(Component.translatable("task.exposure.export.status.exporting", id, exported, total));

            try {
                String fileName = id + look.getIdSuffix();
                new ImageExporter(exposure, fileName)
                        .modify(ImageEffect.chain(
                                look.getModifier(),
                                ImageEffect.Resize.multiplier(size.getMultiplier())
                        ))
                        .toExposuresFolder()
                        .organizeByWorld(Config.Client.EXPORT_ORGANIZE_BY_WORLD.get())
                        .setCreationDate(exposure.getTag().unixTimestamp())
                        .onExport(file -> {
                            exported.getAndIncrement();
                            print(Component.translatable("task.exposure.export.exported")
                                    .append("'" + fileName + "'").withStyle(Style.EMPTY
                                            .withUnderlined(true)
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Open")))
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()))));
                        })
                        .export();
            } catch (Exception e) {
                print(Component.translatable("task.exposure.export.error", id, e.getMessage()));
                LOGGER.error("Failed to export exposure '{}': ", id, e);
            }
        }

        if (exported.get() > 0) {
            print(Component.translatable("task.exposure.export.result", total));
        } else {
            print(Component.translatable("task.exposure.export.none_exported"));
        }

        updateStatus(Component.empty());

        return Result.success(true);
    }

    protected void print(MutableComponent message) {
        Minecrft.execute(() -> Minecrft.playerOrThrow().displayClientMessage(message, false));
    }

    protected void updateStatus(MutableComponent status) {
        Minecrft.execute(() -> Minecrft.playerOrThrow().displayClientMessage(status, true));
    }
}