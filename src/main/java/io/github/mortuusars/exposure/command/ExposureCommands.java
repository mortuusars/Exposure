package io.github.mortuusars.exposure.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.ClientboundExposeCommandPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class ExposureCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exposure")
                .requires((stack) -> stack.hasPermission(2))
                .then(Commands.literal("expose")
                        .executes(context -> takeScreenshot(context.getSource(), Integer.MAX_VALUE))
                        .then(Commands.argument("size", IntegerArgumentType.integer(1, 9999))
                                .executes(context -> takeScreenshot(context.getSource(), IntegerArgumentType.getInteger(context, "size"))))));
    }

    private static int takeScreenshot(CommandSourceStack stack, int size) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(new ClientboundExposeCommandPacket(size, false), player);
        return 0;
    }
}
