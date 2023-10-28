package io.github.mortuusars.exposure.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.ExposureSender;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ExposeCommandClientboundPacket;
import io.github.mortuusars.exposure.network.packet.LoadExposureCommandClientboundPacket;
import io.github.mortuusars.exposure.network.packet.ShowExposureClientboundPacket;
import io.github.mortuusars.exposure.storage.ExposureSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ExposureCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exposure")
                .requires((stack) -> stack.hasPermission(2))
                .then(Commands.literal("load")
                        .then(Commands.literal("withDithering")
                                .then(Commands.argument("size", IntegerArgumentType.integer(1, 2048))
                                        .then(Commands.argument("path", StringArgumentType.string())
                                                .then(Commands.argument("id", StringArgumentType.string())
                                                        .executes(context -> loadExposureFromFile(context.getSource(),
                                                                StringArgumentType.getString(context, "id"),
                                                                StringArgumentType.getString(context, "path"),
                                                                IntegerArgumentType.getInteger(context, "size"), true))))))
                        .then(Commands.argument("size", IntegerArgumentType.integer(1, 2048))
                                .then(Commands.argument("path", StringArgumentType.string())
                                        .then(Commands.argument("id", StringArgumentType.string())
                                                .executes(context -> loadExposureFromFile(context.getSource(),
                                                        StringArgumentType.getString(context, "id"),
                                                        StringArgumentType.getString(context, "path"),
                                                        IntegerArgumentType.getInteger(context, "size"), false))))))
                .then(Commands.literal("expose")
                        .executes(context -> takeScreenshot(context.getSource(), Integer.MAX_VALUE))
                        .then(Commands.argument("size", IntegerArgumentType.integer(1, 2048))
                                .executes(context -> takeScreenshot(context.getSource(), IntegerArgumentType.getInteger(context, "size")))))
                .then(Commands.literal("show")
                        .then(Commands.argument("exposure_id", StringArgumentType.string())
                                .executes(context -> showExposure(context.getSource(),
                                        StringArgumentType.getString(context, "exposure_id"), false))
                                .then(Commands.literal("negative")
                                        .executes(context -> showExposure(context.getSource(),
                                                StringArgumentType.getString(context, "exposure_id"), true))))));
    }

    private static int showExposure(CommandSourceStack stack, String exposureId, boolean negative) {
        ServerPlayer player = stack.getPlayer();
        if (player == null) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_a_player"));
            return 0;
        }

        Optional<ExposureSavedData> exposureData = Exposure.getStorage().getOrQuery(exposureId);
        if (exposureData.isEmpty()) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_found", exposureId));
            return 0;
        }

        ExposureSender.sendToClient(exposureId, exposureData.get(), player);
        Packets.sendToClient(new ShowExposureClientboundPacket(exposureId, negative), player);

        return 0;
    }

    private static int loadExposureFromFile(CommandSourceStack stack, String id, String path, int size, boolean dither) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(new LoadExposureCommandClientboundPacket(id, path, size, dither), player);
        return 0;
    }

    private static int takeScreenshot(CommandSourceStack stack, int size) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(new ExposeCommandClientboundPacket(size), player);
        return 0;
    }
}
