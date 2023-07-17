package io.github.mortuusars.exposure.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ClientboundExposeCommandPacket;
import io.github.mortuusars.exposure.network.packet.ClientboundLoadExposureCommandPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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
                                .executes(context -> takeScreenshot(context.getSource(), IntegerArgumentType.getInteger(context, "size"))))));
    }

    private static int loadExposureFromFile(CommandSourceStack stack, String id, String path, int size, boolean dither) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(new ClientboundLoadExposureCommandPacket(id, path, size, dither), player);
        return 0;
    }

    private static int takeScreenshot(CommandSourceStack stack, int size) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(new ClientboundExposeCommandPacket(size), player);
        return 0;
    }
}
