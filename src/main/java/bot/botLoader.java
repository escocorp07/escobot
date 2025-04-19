package main.java.bot;
// discord4j

import arc.util.Log;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import main.java.BVars;
import main.java.bot.commands.botCommands;
import reactor.core.publisher.Mono;

import static main.java.BVars.*;
import static main.java.bot.commands.commandHandler.handleEvent;

public class botLoader {
    public static void load() {
        DiscordClient client = DiscordClient.create(btoken);
        Log.info("Bot loaded!");
        BVars.login = client.withGateway(gw -> {
            Log.info("Gateway connected!");
            botCommands.registerCommands();
            BVars.gateway = gw;
            gw.on(ReactionAddEvent.class, event -> {
                event.getMessage().flatMap(m->{
                    if(m.getId().asLong() == reactionMessage) {
                        ReactionEmoji.Unicode s = event.getEmoji().asUnicodeEmoji().orElse(null);
                        if(s != null)
                            if(s.getRaw().equals("\uD83D\uDDDE\uFE0F")) {
                                m.getAuthor().ifPresent(a -> {
                                    a.asMember(guild).flatMap(mem -> {
                                        mem.addRole(Snowflake.of(newsid), "Reaction add.").subscribe();
                                        return Mono.empty();
                                    }).subscribe();
                                });
                            }
                    }
                    return Mono.empty();
                }).subscribe();
                return Mono.empty();
            }).doOnError(errorLogger::logErr).onErrorResume(e->{
                errorLogger.logErr(e);
                return Mono.empty();
            });
            gw.on(ReactionRemoveEvent.class, event -> {
                event.getMessage().flatMap(m->{
                    if(m.getId().asLong() == reactionMessage) {
                        ReactionEmoji.Unicode s = event.getEmoji().asUnicodeEmoji().orElse(null);
                        if(s != null)
                            if(s.getRaw().equals("\uD83D\uDDDE\uFE0F")) {
                                m.getAuthor().ifPresent(a -> {
                                    a.asMember(guild).flatMap(mem -> {
                                        mem.removeRole(Snowflake.of(newsid), "Reaction remove.").subscribe();
                                        return Mono.empty();
                                    }).subscribe();
                                });
                            }
                    }
                    return Mono.empty();
                }).subscribe();
                return Mono.empty();
            }).doOnError(errorLogger::logErr).onErrorResume(e->{
                errorLogger.logErr(e);
                return Mono.empty();
            });
            return gw.on(MessageCreateEvent.class, event -> {
                handleEvent(event);
                return Mono.empty();
            }).doOnError(errorLogger::logErr).onErrorResume(e->{
                errorLogger.logErr(e);
                return Mono.empty();
            });
        });
        BVars.login.block();
    }
}