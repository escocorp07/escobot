package main.java.bot.commands;

import arc.util.Log;
import arc.util.Threads;
import discord4j.core.object.reaction.ReactionEmoji;
import main.java.bot.botUtils;
import main.java.bot.errorLogger;
import mindustry.net.ArcNetProvider;
import mindustry.net.Net;

import java.util.Arrays;

import static main.java.bot.botUtils.sendMessage;
import static main.java.bot.commands.commandHandler.registerCommand;

public class botCommands {
    private static boolean loaded = false;
    public static void registerCommands() {
        loaded = true;
        Log.info("Creating commands.");
        registerCommand("test", "Just test command", (e, args)->{
            sendMessage(e.getMessage().getChannelId(), Arrays.toString(args));
        });
        registerCommand("status", "Check server status.", (e, args)->{
            if(args.length != 2) {
                sendMessage(e.getMessage().getChannelId(), "Args: <ip> <port>");
                return;
            }

            e.getMessage().getChannel().subscribe(ch->ch.type().subscribe());
            Threads.daemon(()->{
                new Net(new ArcNetProvider()).pingHost(args[0], Integer.parseInt(args[1]), host->{
                    sendMessage(e.getMessage().getChannelId(), "Server name: " + host.name.replace("omnicorp", "omniporn")+"\nPlayers: "+host.players+"/"+host.playerLimit+"\nMode Name: "+host.modeName+"\nPing: "+host.ping+"\nMap: "+host.mapname);
                }, err->{
                    sendMessage(e.getMessage().getChannelId(), "Im only received error, logged to file.");
                    e.getMessage().addReaction(ReactionEmoji.unicode("❌")).subscribe();
                    errorLogger.logErr(err);
                });
            });
        });
    }
}
