package io.github.mortuusars.exposure.commands.exposure;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.data.export.ExportSize;
import io.github.mortuusars.exposure.commands.argument.ExposureLookArgument;
import io.github.mortuusars.exposure.commands.argument.SizeMultiplierArgument;
import io.github.mortuusars.exposure.commands.suggestion.ExposureIdSuggestionProvider;
import io.github.mortuusars.exposure.data.export.ExportLook;
import io.github.mortuusars.mortaar.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.ExportS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ExportStopS2CP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ExportCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return literal("export")
                .requires((stack) -> stack.hasPermission(3))
                .then(id())
                .then(all())
                .then(literal("stop")
                        .executes(context -> {
                            Packets.sendToClient(ExportStopS2CP.INSTANCE, context.getSource().getPlayerOrException());
                            return 0;
                        }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> id() {
        return literal("id")
                .then(argument("id", StringArgumentType.string())
                        .suggests(new ExposureIdSuggestionProvider())
                        .executes(context -> exportExposures(context.getSource(),
                                List.of(StringArgumentType.getString(context, "id")),
                                ExportSize.X1,
                                ExportLook.REGULAR))
                        .then(argument("size", new SizeMultiplierArgument())
                                .executes(context -> exportExposures(context.getSource(),
                                        List.of(StringArgumentType.getString(context, "id")),
                                        SizeMultiplierArgument.getSize(context, "size"),
                                        ExportLook.REGULAR))
                                .then(argument("look", new ExposureLookArgument())
                                        .executes(context -> exportExposures(context.getSource(),
                                                List.of(StringArgumentType.getString(context, "id")),
                                                SizeMultiplierArgument.getSize(context, "size"),
                                                ExposureLookArgument.getLook(context, "look"))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> all() {
        return literal("all")
                .executes(context -> exportAll(context.getSource(), ExportSize.X1, ExportLook.REGULAR))
                .then(argument("size", new SizeMultiplierArgument())
                        .executes(context -> exportAll(context.getSource(),
                                SizeMultiplierArgument.getSize(context, "size"),
                                ExportLook.REGULAR))
                        .then(argument("look", new ExposureLookArgument())
                                .executes(context -> exportAll(context.getSource(),
                                        SizeMultiplierArgument.getSize(context, "size"),
                                        ExposureLookArgument.getLook(context, "look")))));
    }

    private static int exportAll(CommandSourceStack stack, ExportSize size, ExportLook look) throws CommandSyntaxException {
        List<String> ids = ExposureServer.exposureRepository().getAllIds();
        return confirmExportAll(stack, ids, size, look) ? exportExposures(stack, ids, size, look) : 0;
    }

    private static final Map<Integer, Long> EXPORT_CONFIRMATIONS = new HashMap<>();

    private static boolean confirmExportAll(CommandSourceStack stack, List<String> ids, ExportSize size, ExportLook look) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        int count = ids.size();

        if (count < 50) return true;

        @Nullable Long timestamp = EXPORT_CONFIRMATIONS.get(player.getId());
        boolean confirmed = timestamp != null && player.level().getGameTime() - timestamp < 6000; // 5 minutes

        if (!confirmed) {
            EXPORT_CONFIRMATIONS.put(player.getId(), player.level().getGameTime());
            stack.sendSuccess(() -> Component.translatable("command.exposure.export.confirmation", count)
                    .withStyle(Style.EMPTY.withColor(0xffe5e3))
                    .append(Component.translatable("command.exposure.export.confirm")
                            .withStyle(Style.EMPTY.withColor(0xff7369)
                                    .withUnderlined(true)
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("command.exposure.export.confirm.tooltip")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/exposure export all " + size.getSerializedName() + " " + look.getSerializedName())))), true);
            return false;
        }

        EXPORT_CONFIRMATIONS.remove(player.getId());
        return true;
    }

    private static int exportExposures(CommandSourceStack stack, List<String> exposureIds, ExportSize size, ExportLook look) throws CommandSyntaxException {
        ServerPlayer player = stack.getPlayerOrException();
        Packets.sendToClient(new ExportS2CP(exposureIds, size, look), player);
        return 0;
    }
}
