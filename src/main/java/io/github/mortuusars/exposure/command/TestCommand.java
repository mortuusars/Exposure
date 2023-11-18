package io.github.mortuusars.exposure.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.test.Tests;
import io.github.mortuusars.exposure.test.framework.TestResult;
import io.github.mortuusars.exposure.test.framework.TestingResult;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class TestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("test")
                        .requires((commandSourceStack -> commandSourceStack.hasPermission(3)))
                        .then(Commands.literal("exposure")
                                .executes(TestCommand::run)));
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            TestingResult testingResult = new Tests(player).run();

            MutableComponent message = Component.literal("Testing: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("Total: " + testingResult.getTotalTestCount() + ".").withStyle(ChatFormatting.WHITE));

            for (TestResult failedTest : testingResult.failed()) {
                if (failedTest.error() != null)
                    context.getSource().sendFailure(Component.literal(failedTest.name() + " failed: " + failedTest.error())
                            .withStyle(ChatFormatting.DARK_RED));
            }

            if (testingResult.passed().size() > 0) {
                message.append(" ");
                message.append(Component.literal("Passed: " + testingResult.passed()
                        .size() + ".").withStyle(ChatFormatting.GREEN));
            }

            if (testingResult.failed().size() > 0) {
                message.append(" ");
                message.append(Component.literal("Failed: " + testingResult.failed()
                        .size() + ".").withStyle(ChatFormatting.RED));
            }

            if (testingResult.skipped().size() > 0) {
                message.append(" ");
                message.append(Component.literal("Skipped: " + testingResult.skipped()
                        .size() + ".").withStyle(ChatFormatting.GRAY));
            }

            if (testingResult.failed().size() == 0)
                context.getSource().sendSuccess(message, false);
            else
                context.getSource().sendFailure(message);

        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
}
