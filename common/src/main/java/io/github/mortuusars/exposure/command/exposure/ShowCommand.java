package io.github.mortuusars.exposure.command.exposure;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.storage.ExposureSavedData;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.client.ShowExposureS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ShowCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("show")
                .then(Commands.literal("latest")
                        .executes(context -> latest(context.getSource(), false))
                        .then(Commands.literal("negative")
                                .executes(context -> latest(context.getSource(), true))))
                .then(Commands.literal("exposure")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(context -> exposureId(context.getSource(),
                                        StringArgumentType.getString(context, "id"), false))
                                .then(Commands.literal("negative")
                                        .executes(context -> exposureId(context.getSource(),
                                                StringArgumentType.getString(context, "id"), true)))))
                .then(Commands.literal("texture")
                        .then(Commands.argument("path", StringArgumentType.string())
                                .executes(context -> texture(context.getSource(),
                                        StringArgumentType.getString(context, "path"), false))
                                .then(Commands.literal("negative")
                                        .executes(context -> texture(context.getSource(),
                                                StringArgumentType.getString(context, "path"), true)))));
    }

    private static int latest(CommandSourceStack stack, boolean negative) {
        ServerPlayer player = stack.getPlayer();
        if (player == null) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_a_player"));
            return 1;
        }

        Packets.sendToClient(ShowExposureS2CP.latest(negative), player);
        return 0;
    }

    private static int exposureId(CommandSourceStack stack, String id, boolean negative) {
        ServerPlayer player = stack.getPlayer();
        if (player == null) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_a_player"));
            return 1;
        }

        Optional<ExposureSavedData> exposureData = ExposureServer.getExposureStorage().getOrQuery(id);
        if (exposureData.isEmpty()) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_found", id));
            return 0;
        }

        ExposureServer.getExposureSender().sendTo(player, id, exposureData.get());

        Packets.sendToClient(ShowExposureS2CP.id(id, negative), player);

        return 0;
    }

    private static int texture(CommandSourceStack stack, String path, boolean negative) {
        ServerPlayer player = stack.getPlayer();
        if (player == null) {
            stack.sendFailure(Component.translatable("command.exposure.show.error.not_a_player"));
            return 1;
        }

        Packets.sendToClient(ShowExposureS2CP.texture(path, negative), player);

        return 0;
    }
}
