package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;


public class HomeCommand {
    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext dedicated,
            CommandSelection environment) {
                HomeHandler handler = new HomeHandler();

        dispatcher.register(
                Commands.literal("home")
                        .then(Commands.literal("go")
                                .then(Commands.argument("target", StringArgumentType.string())
                                        .executes(context -> handler.dispatcher("go", context)))));

        dispatcher.register(
                Commands.literal("home")
                        .then(Commands.literal("add")
                                .then(Commands.argument("target", StringArgumentType.string())
                                        .executes(context -> handler.dispatcher("add", context)))));

        dispatcher.register(
                Commands.literal("home")
                        .then(Commands.literal("delete")
                                .then(Commands.argument("target", StringArgumentType.string())
                                        .executes(context -> handler.dispatcher("delete", context)))));

        dispatcher.register(
                Commands.literal("home")
                        .then(Commands.literal("list")
                                .executes(context -> handler.dispatcher("list", context))));
    }
}
