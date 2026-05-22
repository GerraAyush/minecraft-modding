package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;

public class SuggestionsMod {
    // A SuggestionProvider is used to make a list of suggestions that will be sent
    // to the client. A suggestion provider is a functional interface that takes a
    // CommandContext and a SuggestionBuilder and returns some Suggestions. The
    // SuggestionProvider returns a CompletableFuture as the suggestions may not be
    // available immediately.

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registryAccess,
            CommandSelection environment) {
        dispatcher.register(Commands.literal("command_with_suggestions").then(
                Commands.argument("entity", ResourceArgument.resource(registryAccess, Registries.ENTITY_TYPE))
                        .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
                        .executes(SuggestionsMod::executeCommandWithSuggestions)));

        dispatcher.register(Commands.literal("command_with_custom_suggestions").then(
                Commands.argument("player_name", StringArgumentType.string())
                        .suggests(new PlayerSuggestionProvider())
                        .executes(SuggestionsMod::executeCommandWithCustomSuggestions)));
    }

    private static int executeCommandWithSuggestions(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        var entityType = ResourceArgument.getSummonableEntityType(context, "entity");
        context.getSource().sendSuccess(() -> Component.literal(
                "Called /command_with_suggestions with entity = %s".formatted(entityType.value().toShortString())),
                false);
        return 1;
    }

    private static int executeCommandWithCustomSuggestions(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "player_name");
        context.getSource().sendSuccess(
                () -> Component.literal("Called /command_with_custom_suggestions with value = %s".formatted(name)),
                false);
        return 1;
    }
}
