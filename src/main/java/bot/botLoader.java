package main.java.bot;
// discord4j

import arc.util.Log;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.*;
import discord4j.core.event.domain.guild.*;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.gateway.GatewayOptions;
import discord4j.gateway.intent.IntentSet;
import main.java.BVars;
import main.java.bot.commands.botCommands;
import main.java.bot.emoji.botEmoji;
import reactor.core.publisher.Mono;
import static main.java.bot.join.event.handleJEvent;

import static main.java.bot.emoji.emojiHandler.*;
import static main.java.BVars.*;
import static main.java.bot.commands.commandHandler.handleEvent;

public class botLoader {
    /**Инициализировать бота.*/
    public static void load() {
        DiscordClient client = DiscordClient.create(btoken);
        BVars.client = client;
        Log.info("Bot loaded!");
        GatewayBootstrap<GatewayOptions> gp = client.gateway()
                .setEnabledIntents(IntentSet.all());
        gateway = gp.login().block();
        Log.info("Gateway connected!");
        botCommands.registerCommands();
        botEmoji.registerEmojis();
        gateway.on(ReactionAddEvent.class, event -> {
            handleEmojiEvent(event);
            event.getMessage().flatMap(m->{
                if(m.getId().asLong() == reactionMessage) {
                    ReactionEmoji.Unicode s = event.getEmoji().asUnicodeEmoji().orElse(null);
                    if(s != null) {
                        if (s.getRaw().equals("\uD83D\uDDDE\uFE0F")) {
                            event.getUser().flatMap(a -> {
                                a.asMember(guild).flatMap(mem -> {
                                    errorLogger.debug("Adding role.");
                                    mem.addRole(Snowflake.of(newsid), "Reaction add.").subscribe();
                                    return Mono.empty();
                                }).subscribe();
                                return Mono.empty();
                            }).subscribe();
                        } else {
                            errorLogger.debug("emoji is not news paper");
                        }
                    } else {
                        errorLogger.debug("emoji is null");
                    }
                } else {
                    errorLogger.debug("Not reaction message");
                }
                return Mono.empty();
            }).subscribe();
            return Mono.empty();
        }).subscribe();
        gateway.on(ReactionRemoveEvent.class, event -> {
            handleEmojiEvent(event);
            event.getMessage().flatMap(m->{
                if(m.getId().asLong() == reactionMessage) {
                    ReactionEmoji.Unicode s = event.getEmoji().asUnicodeEmoji().orElse(null);
                    if(s != null) {
                        if (s.getRaw().equals("\uD83D\uDDDE\uFE0F")) {
                            event.getUser().flatMap(a -> {
                                a.asMember(guild).flatMap(mem -> {
                                    errorLogger.debug("Removing role.");
                                    mem.removeRole(Snowflake.of(newsid), "Reaction remove.").subscribe();
                                    return Mono.empty();
                                }).subscribe();
                                return Mono.empty();
                            }).subscribe();
                        } else {
                            errorLogger.debug("Not news paper.");
                        }
                    } else {
                        errorLogger.debug("emoji is null.");
                    }
                } else {
                    errorLogger.debug("Not reaction message");
                }
                return Mono.empty();
            }).subscribe();
            return Mono.empty();
        }).subscribe();
        gateway.on(MemberJoinEvent.class, event -> {
            errorLogger.debug("Event received!");
            handleJEvent(event);
            return Mono.empty();
        }).subscribe();
        gateway.on(MessageCreateEvent.class, event -> {
            handleEvent(event);
            return Mono.empty();
        }).subscribe();
        gateway.getEventDispatcher().on(MemberJoinEvent.class, event->{
            handleJEvent(event);
            return Mono.empty();
        }).subscribe();
        //BVars.login.block();
    }
}