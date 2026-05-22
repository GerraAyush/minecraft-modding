package com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class TpaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpa")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> sendTeleportRequest(context))));

        dispatcher.register(Commands.literal("tpaccept")
                .executes(context -> {
                    TpaHandler.acceptTeleport(context.getSource());
                    return 1;
                }));

        dispatcher.register(Commands.literal("tpadeny")
                .executes(context -> {
                    TpaHandler.denyTpaRequest(context.getSource());
                    return 1;
                }));

        dispatcher.register(Commands.literal("back")
                .executes(context -> {
                    TpaHandler.teleportBack(context.getSource());
                    return 1;
                }));

        dispatcher.register(Commands.literal("front")
                .executes(context -> {
                    TpaHandler.teleportFront(context.getSource());
                    return 1;
                }));
    }

    private static int sendTeleportRequest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        TpaHandler.requestTeleport(sender, target);
        return 1;
    }
}
