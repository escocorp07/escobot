package main.java.bot.commands;

import arc.util.Log;
import arc.util.OS;
import main.kotlin.bot.KbotCommands;

import java.util.Arrays;

import static main.java.BVars.*;
import static main.java.bot.botUtils.sendMessage;
import static main.java.bot.commands.commandHandler.registerCommand;
import static main.java.BuildInfo.*;

public class botCommands {
    private static boolean loaded = false;
    public static void registerCommands() {
        if(loaded) {
            Log.err("Someone trying to register commands when it already loaded!");
            return;
        }
        loaded = true;
        Log.info("Creating commands.");
        registerCommand("test", "Just test command", (e, args)->{
            sendMessage(e.getMessage().getChannelId(), Arrays.toString(args));
        });
        registerCommand("stats", "Bot stats.", (e, args)->{
            StringBuilder sb = new StringBuilder();
            if(OS.username.equals("container")) {
                sb.append("Running from docker\n");
            } else {
                sb.append("Running from "+OS.username+"\n");
            }
            sb.append("Version: " + GIT_HASH);
            sendMessage(e.getMessage().getChannelId(), sb.toString());
            sb.setLength(0);
        });
        registerCommand("say", "idk.", ownerid, (e, args)->{
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg + " ");
            }
            sendMessage(e.getMessage().getChannelId(), sb.toString());
            sb.setLength(0);
        });
        registerCommand("do", "idk.", grelyid, (e, args)->{
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i<args.length;i++) {
                sb.append(args[i] + " ");
            }
            sendMessage(e.getMessage().getChannelId(), OS.exec(args[0], sb.toString()));
            sb.setLength(0);
        });
        /*registerCommand("status", "Check server status.", (e, args)->{
            if(args.length != 2) {
                sendMessage(e.getMessage().getChannelId(), "Args: <ip> <port>");
                return;
            }

            e.getMessage().getChannel().subscribe(ch->ch.type().subscribe());
            Snowflake id = e.getMessage().getChannelId();
            //Threads.daemon(()->{
            try {
                new Net(new ArcNetProvider()).pingHost(args[0], Integer.parseInt(args[1]), host -> {
                    sendMessage(id, "Server name: " + host.name.replace("omnicorp", "omniporn") + "\nPlayers: " + host.players + "/" + host.playerLimit + "\nMode Name: " + host.modeName + "\nPing: " + host.ping + "\nMap: " + host.mapname);
                }, err -> {
                    sendMessage(id, "I`m only received error, logged to file.");
                    e.getMessage().addReaction(ReactionEmoji.unicode("❌")).subscribe();
                    errorLogger.logErr(err);
                });
            } catch (Exception err) {
                sendMessage(id, "I`m only received error, logged to file.");
                e.getMessage().addReaction(ReactionEmoji.unicode("❌")).subscribe();
                errorLogger.logErr(err);
            }
            //});
        });*/

        KbotCommands.Companion.KregisterCommands();
    }
}
