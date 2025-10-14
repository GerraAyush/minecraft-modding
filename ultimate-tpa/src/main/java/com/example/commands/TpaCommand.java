package main.java.com.example.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource; // Note: ServerCommandSource extends CommandSource
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import com.example.UltimateTpaMod;


public class TpaCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("tpa")
                .then(argument("target", EntityArgumentType.player())
                    .executes(ctx -> {
                        ServerPlayerEntity sender = ctx.getSource().getPlayer();
                        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                        UltimateTpaMod.sendTpaRequest(sender, target);
                        return 1;
                    }))
        );

        dispatcher.register(
            literal("tpaccept")
                .executes(ctx -> {
                    ServerPlayerEntity accepter = ctx.getSource().getPlayer();
                    UltimateTpaMod.acceptTpaRequest(accepter);
                    return 1;
                })
        );

        dispatcher.register(
            literal("tpadeny")
                .executes(ctx -> {
                    ServerPlayerEntity denier = ctx.getSource().getPlayer();
                    UltimateTpaMod.denyTpaRequest(denier);
                    return 1;
                })
        );

        dispatcher.register(
            literal("back")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    UltimateTpaMod.back(player);
                    return 1;
                })
        );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> literal(String name) {
        return CommandManager.literal(name);
    }

    private static <T> RequiredArgumentBuilder<ServerCommandSource, T> argument(String name, ArgumentType<T> type) {
        return CommandManager.argument(name, type);
    }
}
