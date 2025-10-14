package main.java.com.example;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource; // Note: ServerCommandSource extends CommandSource
import net.minecraft.command.argument.EntityArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import com.example.HomeMod;


public class HomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("home")
                .then(literal("go")
                    .then(argument("target", StringArgumentType.string())
                        .executes(ctx -> {
                            ServerPlayerEntity sender = ctx.getSource().getPlayer();
                            String target = StringArgumentType.getString(ctx, "target");
                            HomeMod.goHomeRequest(sender, target);
                            return 1;
                        })
                    )
                )
        );

        dispatcher.register(
            literal("home")
                .then(literal("add")
                    .then(argument("target", StringArgumentType.string())
                        .executes(ctx -> {
                            ServerPlayerEntity sender = ctx.getSource().getPlayer();
                            String target = StringArgumentType.getString(ctx, "target");
                            HomeMod.addHomeRequest(sender, target);
                            return 1;
                        })
                    )
                )
        );

        dispatcher.register(
            literal("home")
                .then(literal("del")
                    .then(argument("target", StringArgumentType.string())
                        .executes(ctx -> {
                            ServerPlayerEntity sender = ctx.getSource().getPlayer();
                            String target = StringArgumentType.getString(ctx, "target");
                            HomeMod.delHomeRequest(sender, target);
                            return 1;
                        })
                    )
                )
        );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> literal(String name) {
        return CommandManager.literal(name);
    }

    private static <T> RequiredArgumentBuilder<ServerCommandSource, T> argument(String name, ArgumentType<T> type) {
        return CommandManager.argument(name, type);
    }
}
