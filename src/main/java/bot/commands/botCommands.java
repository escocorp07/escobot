package main.java.bot.commands;

import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.util.Log;
import arc.util.Threads;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.spec.MessageCreateFields;
import discord4j.discordjson.json.MessageCreateRequest;
import main.kotlin.bot.*;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import main.java.bot.botUtils;
import main.java.bot.errorLogger;
import mindustry.io.MapIO;
import mindustry.net.ArcNetProvider;
import mindustry.net.Net;
import java.io.*;
import mindustry.maps.Map;
import arc.files.Fi;

import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static main.java.bot.botUtils.sendMessage;
import static main.java.bot.commands.commandHandler.registerCommand;
import static main.java.bot.utils.*;

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
