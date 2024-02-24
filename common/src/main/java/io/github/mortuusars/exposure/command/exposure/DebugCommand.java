package io.github.mortuusars.exposure.command.exposure;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ClearRenderingCacheS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class DebugCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("debug")
                .then(Commands.literal("clearRenderingCache")
                        .executes(DebugCommand::clearRenderingCache));
    }

    private static int clearRenderingCache(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(new ClearRenderingCacheS2CP(), player);
        return 0;
    }
}
