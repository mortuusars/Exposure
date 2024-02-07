package io.github.mortuusars.exposure.command.exposure;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ExposeCommandS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class ExposeCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("expose")
                .executes(context -> expose(context.getSource(), Integer.MAX_VALUE))
                .then(Commands.argument("size", IntegerArgumentType.integer(1, 2048))
                        .executes(context -> expose(context.getSource(), IntegerArgumentType.getInteger(context, "size"))));
    }

    private static int expose(CommandSourceStack stack, int size) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(new ExposeCommandS2CP(size), player);
        return 0;
    }
}
