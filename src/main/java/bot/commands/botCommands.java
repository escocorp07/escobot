package main.java.bot.commands;

import arc.util.Log;
import main.java.bot.botUtils;

import static main.java.bot.commands.commandHandler.registerCommand;

public class botCommands {
    private static boolean loaded = false;
    public static void registerCommands() {
        loaded = true;
        Log.info("Creating commands.");
        registerCommand("test", "Just test command", (e, args)->{
            botUtils.sendMessage(e.getMessage().getChannelId(), "It works!");
        });
    }
}
