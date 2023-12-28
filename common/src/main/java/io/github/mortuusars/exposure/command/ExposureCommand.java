package io.github.mortuusars.exposure.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mortuusars.exposure.command.exposure.ExportCommand;
import io.github.mortuusars.exposure.command.exposure.ExposeCommand;
import io.github.mortuusars.exposure.command.exposure.LoadCommand;
import io.github.mortuusars.exposure.command.exposure.ShowCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ExposureCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exposure")
                .requires((stack) -> stack.hasPermission(2))
                .then(LoadCommand.get())
                .then(ExposeCommand.get())
                .then(ExportCommand.get())
                .then(ShowCommand.get()));
    }
}
