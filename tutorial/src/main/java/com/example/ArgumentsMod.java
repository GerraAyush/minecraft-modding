package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.network.chat.Component;

public class ArgumentsMod {
    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext dedicated,
            CommandSelection environment) {

        dispatcher.register(Commands.literal("command_with_arg")
                .then(Commands.argument("value", IntegerArgumentType.integer())
                        .executes(ArgumentsMod::executeCommandWithArg)));

        // Optional argument two
        dispatcher.register(Commands.literal("command_with_two_args")
                .then(Commands.argument("value_one", IntegerArgumentType.integer())
                        .executes(ArgumentsMod::executeWithOneArg)
                        .then(Commands.argument("value_two", IntegerArgumentType.integer())
                                .executes(ArgumentsMod::executeWithTwoArgs))));

        // Employ executeCommon() for DRY
        dispatcher.register(Commands.literal("command_with_common_exec")
                .then(Commands.argument("value_one", IntegerArgumentType.integer())
                        .executes(context -> executeCommon(IntegerArgumentType.getInteger(context, "value_one"), 0,
                                context))
                        .then(Commands.argument("value_two", IntegerArgumentType.integer())
                                .executes(context -> executeCommon(
                                        IntegerArgumentType.getInteger(context, "value_one"),
                                        IntegerArgumentType.getInteger(context, "value_two"),
                                        context)))));
    }

    public static int executeCommandWithArg(CommandContext<CommandSourceStack> context) {
        CommandSourceStack sender = context.getSource();

        // Extract the IntegerArgument from context
        int value = IntegerArgumentType.getInteger(context, "value");
        sender.sendSuccess(() -> Component.literal("Called command_with_arg with value = %s".formatted(value)), false);
        return 1;
    }

    public static int executeWithOneArg(CommandContext<CommandSourceStack> context) {
        int value1 = IntegerArgumentType.getInteger(context, "value_one");
        context.getSource().sendSuccess(
                () -> Component.literal("Called /command_with_two_args with value one = %s".formatted(value1)), false);
        return 1;
    }

    private static int executeWithTwoArgs(CommandContext<CommandSourceStack> context) {
        int value1 = IntegerArgumentType.getInteger(context, "value_one");
        int value2 = IntegerArgumentType.getInteger(context, "value_two");
        context.getSource().sendSuccess(
                () -> Component
                        .literal("Called /argtater2 with value one = %s and value two = %s".formatted(value1, value2)),
                false);
        return 1;
    }

    private static int executeCommon(int value1, int value2, CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(
                "Called /command_with_common_exec with value 1 = %s and value 2 = %s".formatted(value1, value2)),
                false);
        return 1;
    }
}

// Custom Argument Types are possible: https://docs.fabricmc.net/develop/commands/arguments