package io.github.mortuusars.exposure.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.commands.argument.ShaderLocationArgument;
import io.github.mortuusars.mortaar.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.ShaderApplyS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShaderCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shader")
                .requires((stack) -> stack.hasPermission(2))
                .then(Commands.literal("apply")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("shader_location", new ShaderLocationArgument())
                                        .executes(ShaderCommand::applyShader))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ShaderCommand::removeShader))));
    }

    private static int applyShader(CommandContext<CommandSourceStack> context) {
        ResourceLocation shaderLocation = ResourceLocationArgument.getId(context, "shader_location");
        for (ServerPlayer targetPlayer : getTargetPlayers(context)) {
            Packets.sendToClient(new ShaderApplyS2CP(shaderLocation), targetPlayer);
        }
        return 0;
    }

    private static int removeShader(CommandContext<CommandSourceStack> context) {
        for (ServerPlayer targetPlayer : getTargetPlayers(context)) {
            Packets.sendToClient(ShaderApplyS2CP.REMOVE, targetPlayer);
            context.getSource().sendSuccess(() -> Component.translatable("command.exposure.shader.removed"), false);
        }
        return 0;
    }

    private static List<ServerPlayer> getTargetPlayers(CommandContext<CommandSourceStack> context) {
        try {
            return new ArrayList<>(EntityArgument.getPlayers(context, "targets"));
        } catch (CommandSyntaxException e) {
            return Collections.emptyList();
        }
    }
}
