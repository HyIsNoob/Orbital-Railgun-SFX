package io.github.hyisnoob.railgunsounds.registry;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.hyisnoob.railgunsounds.config.ServerConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandRegistry {
    private static final ServerConfig CONFIG = new ServerConfig();

    private static int showHelp(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("Available commands:\n" +
                "/orsounds debug <true|false> - Toggle debug mode\n" +
                "/orsounds help - List all available commands\n"), false);
        return 1;
    }

    public static void registerCommands() {
        CONFIG.loadConfig();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("orsounds")
                    .executes(CommandRegistry::showHelp)
                    .then(CommandManager.literal("debug")
                            .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                    .executes(context -> toggleDebugMode(context, BoolArgumentType.getBool(context, "enabled")))))
                    .then(CommandManager.literal("help")
                            .executes(CommandRegistry::showHelp)));
        });
    }

    private static int toggleDebugMode(CommandContext<ServerCommandSource> context, boolean enabled) {
        CONFIG.setDebugMode(enabled);
        context.getSource().sendFeedback(() -> Text.literal("Debug mode set to: " + enabled), false);
        return 1;
    }

    public static boolean isDebugMode() {
        return CONFIG.isDebugMode();
    }
}
