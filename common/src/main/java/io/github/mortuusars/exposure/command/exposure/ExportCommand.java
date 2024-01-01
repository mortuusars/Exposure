package io.github.mortuusars.exposure.command.exposure;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.camera.capture.component.FileSaveComponent;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ExportCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("export")
                .requires((stack) -> stack.hasPermission(3))
                .then(Commands.literal("id")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(context -> exportExposures(context.getSource(),
                                        List.of(StringArgumentType.getString(context, "id"))))))
                .then(Commands.literal("all")
                        .executes(context ->
                                exportAll(context.getSource())));
    }

    private static int exportAll(CommandSourceStack source) {
        List<String> ids = ExposureServer.getExposureStorage().getAllIds();
        return exportExposures(source, ids);
    }

    private static int exportExposures(CommandSourceStack stack, List<String> exposureIds) {
        int savedCount = 0;

        File folder = stack.getServer().getWorldPath(LevelResource.ROOT).resolve("exposures").toFile();
        boolean ignored = folder.mkdirs();

        for (String id : exposureIds) {
            Optional<ExposureSavedData> data = ExposureServer.getExposureStorage().getOrQuery(id);
            if (data.isEmpty()) {
                stack.sendFailure(Component.translatable("command.exposure.export.failure.not_found", id));
                continue;
            }

            ExposureSavedData exposureSavedData = data.get();

            boolean saved = new FileSaveComponent(id, folder.getAbsolutePath(), false)
                    .save(exposureSavedData.getPixels(), exposureSavedData.getWidth(), exposureSavedData.getHeight(),
                            exposureSavedData.getProperties());

            if (saved)
                stack.sendSuccess(Component.translatable("command.exposure.export.success.saved_exposure_id", id), true);

            savedCount++;
        }

        if (savedCount > 0) {
            String folderPath = getFolderPath(folder);
            Component folderComponent = Component.literal(folderPath)
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(arg -> arg.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, folderPath)));
            Component component = Component.translatable("command.exposure.export.success.result", savedCount, folderComponent);
            stack.sendSuccess(component, true);
        } else
            stack.sendFailure(Component.translatable("command.exposure.export.failure.none_saved"));

        return 0;
    }

    @NotNull
    private static String getFolderPath(File folder) {
        String folderPath;
        try {
            folderPath = folder.getCanonicalPath();
        } catch (IOException e) {
            LogUtils.getLogger().error(e.toString());
            folderPath = folder.getAbsolutePath();
        }
        return folderPath;
    }
}
