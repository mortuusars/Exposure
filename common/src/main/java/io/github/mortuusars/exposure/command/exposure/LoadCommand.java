package io.github.mortuusars.exposure.command.exposure;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.LoadExposureCommandS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class LoadCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("load")
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
                                                IntegerArgumentType.getInteger(context, "size"), false)))));
    }

    private static int loadExposureFromFile(CommandSourceStack stack, String id, String path, int size, boolean dither) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(new LoadExposureCommandS2CP(id, path, size, dither), player);
        return 0;
    }
}
