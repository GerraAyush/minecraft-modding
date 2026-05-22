package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.network.chat.Component;

public class CommandsMod {
    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext dedicated,
            CommandSelection environment) {

        dispatcher.register(
                Commands.literal("command_text")
                        .executes(context -> {
                            // You can access the command source from a command context by calling
                            // getSource() on the CommandContext instance.
                            CommandSourceStack source = context.getSource();

                            // Instead of passing a finished object (like a fully generated string or a
                            // texture) directly into a method, you pass a Supplier. The Supplier holds
                            // the instruction on how to build the object. The game then calls its .get()
                            // method only when it actually needs to use the object. Its primary purpose
                            // is lazy evaluation—preventing the game from loading heavy data until it
                            // is strictly necessary.

                            // sendSuccess(Supplier<Component>, boolean): Supplier is used to send messages
                            // on chat. <boolean> is for broadcasting the result to other mods, if true
                            // then broadcast otherwise don't.
                            source.sendSuccess(() -> Component.literal("Called /command_text."), false);
                            return 1;
                        }));

        if (environment.includeDedicated) {
            dispatcher.register(Commands.literal("dedicated_command")
                    .executes(CommandsMod::executeDedicatedCommand));
        }

        dispatcher.register(Commands.literal("command_one")
					.then(Commands.literal("sub_command_one").executes(CommandsMod::executeSubCommandOne)));

        // Optional sub_command demonstration
        dispatcher.register(Commands.literal("command_two")
					.executes(CommandsMod::executeCommandTwo)
					.then(Commands.literal("sub_command_two").executes(CommandsMod::executeSubCommandTwo)));
    }

    
	public static int executeDedicatedCommand(CommandContext<CommandSourceStack> context) {
		CommandSourceStack sender = context.getSource();
		sender.sendSuccess(() -> Component.literal("Called /dedicated_command"), false);
		return 1;
	}

	public static int executeSubCommandOne(CommandContext<CommandSourceStack> context) {
		CommandSourceStack sender = context.getSource();
		sender.sendSuccess(() -> Component.literal("Called /command_one sub_command_one"), false);
		return 1;
	}

	public static int executeCommandTwo(CommandContext<CommandSourceStack> context) {
		CommandSourceStack sender = context.getSource();
		sender.sendSuccess(() -> Component.literal("Called /command_two"), false);
		return 1;
	}

	public static int executeSubCommandTwo(CommandContext<CommandSourceStack> context) {
		CommandSourceStack sender = context.getSource();
		sender.sendSuccess(() -> Component.literal("Called /command_two sub_command_two"), false);
		return 1;
	}
}


/*
 * Component vs Command Literals
 * 1. Component (net.minecraft.text.Text)Components are used to display styled
 * text, chat messages, or UI elements in-game. Unlike standard Java Strings,
 * components support colors, formatting (bold, italic), and interactive events
 * (e.g., clicking to run a command or hovering for a tooltip). Usage: Sending
 * chat messages, setting item names, or writing on signs.
 * 
 * Fabric Example:
 * // Creating a simple, colored literal text component
 * Text message = Text.literal("Hello, world!").formatted(Formatting.RED);
 * player.sendMessage(message);
 * Use code with caution.
 * 
 * 2. Command Literal (CommandManager.literal)Command literals are the static
 * words typed into the Minecraft chat to trigger or navigate a command. They
 * define the structure of the command tree in the Brigadier library. Usage:
 * Defining the root command (e.g., /mycommand) or static sub-commands (e.g.,
 * /mycommand reload).
 * 
 * Fabric Example:
 * // Registering a command with a literal root ("teleportme") and an execution
 * // block
 * dispatcher.register(CommandManager.literal("teleportme")
 *   .executes(context -> {
 *     // Command logic here
 *     return 1;
 *   })
 * );
 */
