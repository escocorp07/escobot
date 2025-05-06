package main.java.bot;
// discord4j

import arc.util.Log;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.*;
import discord4j.core.event.domain.guild.*;
import discord4j.core.object.emoji.UnicodeEmoji;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.ThreadChannel;
// import discord4j.core.object.reaction.ReactionEmoji; // deprecated
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.gateway.GatewayOptions;
import discord4j.gateway.intent.IntentSet;
import main.java.BVars;
import main.java.Database.DatabaseConnector;
import main.java.appeals.AppealStatus;
import main.java.bot.commands.botCommands;
import main.java.bot.emoji.botEmoji;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

import static main.java.Database.DatabaseConnector.*;
import static main.java.bot.botUtils.sendMessage;
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
        gateway.updatePresence(ClientPresence.doNotDisturb(ClientActivity.playing(presence))).subscribe();
        Log.info("Gateway connected!");
        botCommands.registerCommands();
        botEmoji.registerEmojis();
        gateway.on(ReactionAddEvent.class, event -> {
            handleEmojiEvent(event);
            event.getMessage().flatMap(m->{
                if(m.getId().asLong() == reactionMessage) {
                    UnicodeEmoji s = event.getEmoji().asUnicodeEmoji().orElse(null);
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
                    Optional<DatabaseConnector.Appeal> appeal = getAppealByMessage(m.getId().asString());
                    appeal.ifPresent(a->{
                        UnicodeEmoji s = event.getEmoji().asUnicodeEmoji().orElse(null);
                        if(s != null) {
                            if(s.getRaw().equals("✅")) {
                                setAppealStatus(a.getId(), AppealStatus.approved.toString());
                            } else if (s.getRaw().equals("❌")) {
                                setAppealStatus(a.getId(), AppealStatus.denied.toString());
                            }
                        }
                    });
                }
                return Mono.empty();
            }).subscribe();
            return Mono.empty();
        }).subscribe();
        gateway.on(ReactionRemoveEvent.class, event -> {
            handleEmojiEvent(event);
            event.getMessage().flatMap(m->{
                if(m.getId().asLong() == reactionMessage) {
                    UnicodeEmoji s = event.getEmoji().asUnicodeEmoji().orElse(null);
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
            User us = event.getMessage().getAuthor().orElse(null);
            if(us == null)
                return Mono.empty();
            if(us.isBot())
                return Mono.empty();
            handledMessages+=1;
            event.getMessage().getChannel().flatMap(ch->{
                if (ch instanceof ThreadChannel threadChannel) {
                    threadChannel.join().subscribe();
                }
                return Mono.empty();
            }).subscribe();
            event.getMessage().getMessageReference().ifPresent(m->{
                //Log.info("Message reference");
                m.getMessageId().ifPresent(mid->{
                    //Log.info("message id");
                    //Log.info(mid.asString());
                    getAppealByMessage(mid.asString()).ifPresent(appeal -> {
                        //Log.info("appeal");
                        if(appeal.getDiscord_message().equals(mid.asString())) {
                            setAppealComment(event.getMessage().getContent(), appeal.getId());
                            //Log.info("set comment");
                            event.getMessage().addReaction(Emoji.unicode("✅")).subscribe();
                        }
                    });
                });
            });
            handleEvent(event);
            if (event.getMessage().getContent().toLowerCase().contains("здарова")) {
                File image = new File("images/здарова.png");
                if (image.exists()) {
                    event.getMessage().getChannel()
                            .flatMap(channel -> {
                                try {
                                    return channel.createMessage(MessageCreateSpec.builder()
                                            .addFile("здарова.png", new FileInputStream(image))
                                            .build());
                                } catch (FileNotFoundException e) {
                                    errorLogger.logErr(e);
                                    return Mono.empty();
                                }
                            })
                            .subscribe();
                }
            }
                    if (event.getMessage().getContent().toLowerCase().contains("1984")) {
                        File image = new File("images/1984.gif");
                        if (image.exists()) {
                            event.getMessage().getChannel()
                                    .flatMap(channel -> {
                                        try {
                                            return channel.createMessage(MessageCreateSpec.builder()
                                                    .addFile("1984.gif", new FileInputStream(image))
                                                    .build());
                                        } catch (FileNotFoundException e) {
                                            errorLogger.logErr(e);
                                            return Mono.empty();
                                        }
                                    })
                                    .subscribe();
                        }
                    }
            return Mono.empty();
        })
                .doOnError(errorLogger::logErr)
                .subscribe();
        gateway.onDisconnect().doFinally(t->{
            Log.info("Bot disconnected!");
        }).block();
    }
}