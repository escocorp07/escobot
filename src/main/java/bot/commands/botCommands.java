package main.java.bot.commands;

import arc.util.Log;
import arc.util.OS;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.VoiceChannelJoinSpec;
import discord4j.rest.util.Color;
import main.java.bot.errorLogger;
import main.kotlin.bot.KbotCommands;
import mindustry.Vars;
import mindustry.core.Version;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static main.java.BVars.*;
import static main.java.bot.botUtils.sendMessage;
import static main.java.bot.commands.commandHandler.registerCommand;
import static main.java.BuildInfo.*;
import static main.java.BVars.*;

public class botCommands {
    private static boolean loaded = false;
    /**Зарегестрировать команды.*/
    public static void registerCommands() {
        if(loaded) {
            Log.err("Someone trying to register commands when it already loaded!");
            return;
        }
        loaded = true;
        Log.info("Time to create commands!");
        registerCommand("stats", "Bot stats.", (e, args)->{
            StringBuilder sb = new StringBuilder();
            if(OS.username.equals("container")) {
                sb.append("Running from docker\n");
            } else {
                sb.append("Running from "+OS.username+"\n");
            }
            sb.append("Version: " + GIT_HASH+"\n");
            sb.append("Mindustry version: " + mindustry_version+"\n");
            sb.append("Commands handled: "+handledCommands+"\n");
            sb.append("Messages handled: "+handledMessages+"\n");
            sendMessage(e.getMessage().getChannelId(), sb.toString());
            sb.setLength(0);
        });
        registerCommand("say", "Сказать от имени бота.", ownerid, (e, args)->{
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg + " ");
            }
            sendMessage(e.getMessage().getChannelId(), sb.toString());
            sb.setLength(0);
        });
        /*registerCommand("join", "idk.", ownerid, (e, args) -> {
            e.getMessage().getAuthor().ifPresent(author -> {
                author.asMember(guild).flatMap(member ->
                        member.getVoiceState().flatMap(state ->
                                state.getChannel().flatMap(channel -> {
                                    VoiceChannelJoinSpec joinSpec = VoiceChannelJoinSpec.builder()
                                            .selfDeaf(true)
                                            .selfMute(false)
                                            .build();

                                    return channel.join(joinSpec);
                                })
                        )
                ).subscribe();
            });
        });*/
        registerCommand("suggest", "Предложить идею", (e, args)->{
            if(args.length < 3) {
                sendMessage(e.getMessage().getChannelId(), "Ваше сообщение содержит слишком мало слов ! (3 - минимум)");
                return;
            }
            EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder()
                    .color(Color.GREEN);
            if(e.getMessage().getAuthor().orElse(null) != null) {
                User u = e.getMessage().getAuthor().orElse(null);
                embed=embed.author(u.getUsername(), /*empty url*/"", u.getAvatarUrl());
            }
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg + " ");
            }
            StringBuilder url = new StringBuilder();
            url.append("https://discord.com/channels/");
            e.getMessage().getGuild().flatMap(g->{
                url.append(g.getId().asString()+"/");
                e.getMessage().getChannel().flatMap(c->{
                    url.append(c.getId().asString()+"/"+e.getMessage().getId().asString());
                    return Mono.empty();
                }).subscribe();
                return Mono.empty();
            }).subscribe();
            embed.addField("", sb.toString()+"\n\n**[Message]("+url.toString()+")**", false);
            MessageCreateSpec.Builder ms = MessageCreateSpec.builder()
                            .addEmbed(embed.build());
            if(!e.getMessage().getAttachments().isEmpty()) {
                for (Attachment a : e.getMessage().getAttachments())
                    if (a.getFilename().toLowerCase().endsWith(".png") ||
                            a.getFilename().toLowerCase().endsWith(".jpg") ||
                            a.getFilename().toLowerCase().endsWith(".jpeg") ||
                            a.getFilename().toLowerCase().endsWith(".webp") ||
                            a.getFilename().toLowerCase().endsWith(".bmp") ||
                            a.getFilename().toLowerCase().endsWith(".tiff") ||
                            a.getFilename().toLowerCase().endsWith(".gif") ||
                            a.getFilename().toLowerCase().endsWith(".mp4") ||
                            a.getFilename().toLowerCase().endsWith(".webm") ||
                            a.getFilename().toLowerCase().endsWith(".mov") ||
                            a.getFilename().toLowerCase().endsWith(".avi") ||
                            a.getFilename().toLowerCase().endsWith(".mkv") ||
                            a.getFilename().toLowerCase().endsWith(".flv") ||
                            a.getFilename().toLowerCase().endsWith(".wmv")) {
                        ms.addEmbed(EmbedCreateSpec.builder().image(a.getUrl()).build());
                    }
            }
                ms.content("<@&1365377730115403837>");
            gateway.getChannelById(Snowflake.of(sugid))
                    .ofType(GuildMessageChannel.class)
                    .flatMap(channel -> channel.createMessage(ms.build()
                    )).subscribe(m->{
                        m.addReaction(ReactionEmoji.unicode("✅")).subscribe();
                        m.addReaction(ReactionEmoji.unicode("❌")).subscribe();
                    });
            sb.setLength(0);
        });
        registerCommand("do", "А тебя это не должно волновать", grelyid, (e, args) -> {
            if (args.length == 0) {
                sendMessage(e.getMessage().getChannelId(), "No command provided.");
                return;
            }
            String cmd = args[0];
            String output;
            if (args.length == 1) {
                try {
                    output = OS.exec(cmd);
                } catch (Exception er) {
                    output = er.getMessage();
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                output = OS.exec(cmd, sb.toString().trim());
            }
            sendMessage(e.getMessage().getChannelId(), output.isEmpty() ? "No output." : output);
        });
        registerCommand("error", "Искуственно создать ошибку.", grelyid, (e, args) -> {
            errorLogger.logErr(new RuntimeException("test"));
        });
        registerCommand("set-join", "Установить сообщение отправляемое участнику по заходу.", grelyid, (e, args) -> {
            if(args.length == 0) {
                joinMessage="";
                sendMessage(e.getMessage().getChannelId(), "Перестал отправлять сообщение при входе.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg + " ");
            }
            joinMessage=sb.toString();
            sendMessage(e.getMessage().getChannelId(), "Новое сообщение: "+sb.toString());
            sb.setLength(0);
        });
        registerCommand("status", "~~Заддосить~~ Проверить статус сервера.", (e, args) -> {
            try {
                Vars.net.pingHost(args[0], Integer.parseInt(args[1]), host -> {
                    sendMessage(e.getMessage().getChannelId(), "Name: " + host.name + "\nPlayers: " + host.players + "/" + host.playerLimit);
                }, er -> {
                    errorLogger.logErr(er);
                    sendMessage(e.getMessage().getChannelId(), "Ошибка при проверке статуса хоста.");
                });
            } catch (Exception err) {
                errorLogger.logErr(err);
            }
        });
        KbotCommands.Companion.KregisterCommands();
    }
}
