package io.github.mortuusars.exposure.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mortuusars.exposure.commands.exposure.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ExposureCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exposure")
                .then(LoadCommand.get())
                .then(ExposeCommand.get())
                .then(ExportCommand.get())
                .then(ShowCommand.get())
                .then(PaletteCommand.get())
                .then(DebugCommand.get()));
    }
}
