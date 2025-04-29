package main.java.bot.commands;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.util.Log;
import arc.util.OS;
import arc.util.Timer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.TopLevelGuildMessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.StartThreadFromMessageSpec;
import discord4j.core.spec.StartThreadFromMessageSpecGenerator;
import discord4j.rest.util.Color;
import main.java.bot.errorLogger;
import main.kotlin.bot.KbotCommands;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static main.java.BVars.*;
import static main.java.bot.botUtils.sendMessage;
import static main.java.bot.botUtils.sendMessageP;
import static main.java.bot.commands.commandHandler.commands;
import static main.java.bot.commands.commandHandler.*;
import static main.java.BuildInfo.*;
import static main.java.BVars.*;
import static main.java.bot.utils.*;

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
        registerCommand("kt", "kt really", ownerid, (e, args)->{
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg + " ");
                }
                String out = "No output";
                try {
                    if(ktsEngine == null)
                        initKotlinScripting();
                    if(ktsEngine == null) {
                        sendMessage(e.getMessage().getChannelId(), "Не могу инициализировать движок!");
                        return;
                    }
                    out = ktsEngine.eval(sb.toString().trim()).toString();
                    // out = ScriptEngineHolder.INSTANCE.getKts().eval(sb.toString().trim()).toString();
                } catch (Exception er) {
                    out = er.getMessage();
                }
                sendMessage(e.getMessage().getChannelId(), out);
                sb.setLength(0);
        });
        registerCommand("js", "js really", ownerid, (e, args)->{
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg + " ");
                }
                String out = "No output";
                try {
                    out = Vars.mods.getScripts().runConsole(sb.toString());
                } catch (Exception er) {
                    out = er.getMessage();
                }
                sendMessage(e.getMessage().getChannelId(), out);
                sb.setLength(0);
        });
        /*
        registerCommand("render", "Render map", ownerid, (e, args)->{
            Message msg = e.getMessage();
            if(msg.getAttachments().size()<1)
                return;
            if(!msg.getAttachments().get(0).getFilename().endsWith(".msav"))
                return;
            getAttach(msg);
            Fi fmap = new Fi("./data/atch/"+msg.getAttachments().get(0).getFilename());
            Map map = getMap(fmap);
            try {
                if(map == null)
                    return;
                Pixmap px = MapIO.generatePreview(map);
                Fi png = new Fi("data/gen/"+msg.getAttachments().get(0).getFilename()+".png");
                PixmapIO.writePng(png, px);
                px.dispose();
                File image = new File("data/gen/"+msg.getAttachments().get(0).getFilename()+".png");
                if (image.exists()) {
                    msg.getChannel()
                            .flatMap(channel -> {
                                try {
                                    return channel.createMessage(MessageCreateSpec.builder()
                                            .addFile("render.png", new FileInputStream(image))
                                            .content("Name: "+map.name()+"\nAuthor: "+map.plainAuthor()+"\nMVersion: v"+map.version+"\nTiles: "+map.width+"x"+map.height)
                                            .build());
                                } catch (FileNotFoundException err) {
                                    errorLogger.logErr(err);
                                    return Mono.empty();
                                }
                            })
                            .subscribe();
                }
            } catch (Exception ex) {
                errorLogger.logErr(ex);
            }
        });*/
        registerCommand("render", "Render map\nYou can use .zip files to do mass render", (e, args) -> {
            Message msg = e.getMessage();
            if (msg.getAttachments().isEmpty())
                return;
            Attachment attachment = msg.getAttachments().get(0);
            String fileName = attachment.getFilename();
            if (fileName.endsWith(".msav")) {
                getAttach(attachment);
                Fi fmap = new Fi("./data/atch/" + fileName);
                Map map = getMap(fmap);
                try {
                    if (map == null)
                        return;
                    Pixmap px = MapIO.generatePreview(map);
                    Fi png = new Fi("data/gen/" + fileName + ".png");
                    PixmapIO.writePng(png, px);
                    px.dispose();
                    File image = new File("data/gen/" + fileName + ".png");
                    if (image.exists()) {
                        msg.getChannel()
                                .flatMap(channel -> {
                                    try {
                                        return channel.createMessage(MessageCreateSpec.builder()
                                                .addFile("render.png", new FileInputStream(image))
                                                /*.content("Name: " + map.name() + "\nAuthor: " + map.plainAuthor() + "\nMVersion: v" + map.version + "\nTiles: " + map.width + "x" + map.height)*/
                                                .addEmbed(EmbedCreateSpec.builder().addField(map.name(), "Author: " + map.plainAuthor() + "\nMVersion: v" + map.version + "\nTiles: " + map.width + "x" + map.height, false).color(Color.GREEN).build())
                                                .build());
                                    } catch (FileNotFoundException err) {
                                        errorLogger.logErr(err);
                                        return Mono.empty();
                                    }
                                })
                                .subscribe();
                    }
                } catch (Exception ex) {
                    errorLogger.logErr(ex);
                }
            } else if (fileName.endsWith(".zip")) {
    getAttach(attachment);
    String destDir = "data/atch/";
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(new File(destDir + fileName)))) {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String entryName = entry.getName();

            if (entryName.endsWith(".msav")) {
                Path normalizedPath = Paths.get(destDir).resolve(entryName).normalize();
                if (!normalizedPath.startsWith(Paths.get(destDir))) {
                    System.out.println("Blocked Zip Slip attempt: " + entryName);
                    continue;
                }

                File outFile = normalizedPath.toFile();
                outFile.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                } catch (IOException io) {
                    errorLogger.logErr(io);
                    continue;
                }

                zis.closeEntry();
                Fi fmap = new Fi(outFile.getPath());
                Map map = getMap(fmap);
                if (map == null) continue;

                try {
                    Pixmap px = MapIO.generatePreview(map);
                    Fi png = new Fi("data/gen/" + entryName + ".png");
                    PixmapIO.writePng(png, px);
                    px.dispose();
                    File image = new File("data/gen/" + entryName + ".png");
                    if (image.exists()) {
                        msg.getChannel()
                            .flatMap(channel -> {
                                try {
                                    return channel.createMessage(MessageCreateSpec.builder()
                                            .addFile("render.png", new FileInputStream(image))
                                            .addEmbed(EmbedCreateSpec.builder()
                                                .addField(map.name(), "Author: " + map.plainAuthor() + "\nMVersion: v" + map.version + "\nTiles: " + map.width + "x" + map.height, false)
                                                .color(Color.GREEN)
                                                .build())
                                            .build());
                                } catch (FileNotFoundException err) {
                                    errorLogger.logErr(err);
                                    return Mono.empty();
                                }
                            })
                            .subscribe();
                    }
                } catch (Exception ex) {
                    errorLogger.logErr(ex);
                }
            }

            zis.closeEntry();
        }
    } catch (IOException ex) {
        errorLogger.logErr(ex);
    }
}
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
        registerCommand("suggest-ban", "Запретить предложку", ownerid, (e, args)->{
            try {
                bannedInSug.add(Snowflake.of(Long.parseLong(args[0])));
                sendMessageP(e.getMessage().getChannelId(), "Добавлено <@"+args[0]+">");
            } catch (Exception err) {
                sendMessage(e.getMessage().getChannelId(), "Не Snowflake!");
            }
        });
        registerCommand("suggest-unban", "Разрешить предложку", ownerid, (e, args)->{
            try {
                bannedInSug.remove(Snowflake.of(Long.parseLong(args[0])));
                sendMessageP(e.getMessage().getChannelId(), "Удалено <@"+args[0]+">");
            } catch (Exception err) {
                sendMessage(e.getMessage().getChannelId(), "Не Snowflake!");
            }
        });
        registerCommand("suggest-banned", "Список людей с запретом предложки", ownerid, (e, args)->{
            StringBuilder sb = new StringBuilder();
            for(Snowflake f : bannedInSug) {
                sb.append("<@" + f.asString() + ">\n");
            }
            sb.setLength(2000);
            sendMessageP(e.getMessage().getChannelId(), sb.toString());
            sb.setLength(0);
        });
        registerCommand("suggest", "Предложить идею", (e, args)->{
            if(args.length < 2) {
                sendMessage(e.getMessage().getChannelId(), "Ваше сообщение содержит слишком мало слов! (2 - минимум)");
                return;
            }
            if(bannedInSug.contains(e.getMessage().getAuthor().orElse(null).getId())) {
                sendMessage(e.getMessage().getChannelId(), "Вам запрещено отправлять предложения!");
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
                ms.content("<@&"+sugpingrole+">");
            gateway.getChannelById(Snowflake.of(sugid))
                    .ofType(GuildMessageChannel.class)
                    .flatMap(channel -> channel.createMessage(ms.build()
                    )).subscribe(m->{
                        m.addReaction(ReactionEmoji.unicode("✅")).subscribe();
                        m.addReaction(ReactionEmoji.unicode("❌")).subscribe();
                        m.startThread(StartThreadFromMessageSpec.builder().name("Обсуждение").reason("Suggest created").build()).subscribe(t->{
                            t.createMessage("Ветка создана.");
                            return;
                        });
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
                output = OS.exec(args);
            }
            sendMessage(e.getMessage().getChannelId(), output.isEmpty() ? "No output." : output);
        });
        registerCommand("error", "Искуственно создать ошибку.", grelyid, (e, args) -> {
            errorLogger.logErr(new RuntimeException("test"));
        });
        registerCommand("help", "Посмотреть команды и их описания", (e, args) -> {
            EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder()
                    .color(Color.GREEN);
            for(botcommand c : commands) {
                embed.addField(c.getName(), c.getDescription(), false);
            }
            MessageCreateSpec.Builder ms = MessageCreateSpec.builder()
                    .addEmbed(embed.build());
            gateway.getChannelById(e.getMessage().getChannelId())
                    .ofType(GuildMessageChannel.class)
                    .flatMap(channel -> channel.createMessage(ms.build()
                    )).subscribe();
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
        /*registerCommand("status", "~~Заддосить~~ Проверить статус сервера.", (e, args) -> {
            try {
                int port = Integer.parseInt(args[1]);
                Vars.net.pingHost(args[0], port, host -> {
                    sendMessage(e.getMessage().getChannelId(), "Name: " + host.name + "\nPlayers: " + host.players + "/" + host.playerLimit);
                }, er -> {
                    errorLogger.logErr(er);
                    sendMessage(e.getMessage().getChannelId(), "Ошибка при проверке статуса хоста.");
                });
                Vars.net.connect(args[0], port, () -> {
                    sendMessage(e.getMessage().getChannelId(), "Connectiong to "+args[0]+":"+port);
                });
                Timer.schedule(() -> {
                    Log.info("finishing connect manually.");
                    finishConnecting();
                }, 2);
                Timer.schedule(()->{
                    sendMessage(e.getMessage().getChannelId(), "Bot nick: "+Vars.player.name);
                    Groups.player.each(p->{
                        sendMessage(e.getMessage().getChannelId(), p.plainName()+" id:"+p.id);
                    });
                    Timer.schedule(()->{
                        Vars.net.setClientLoaded(false);
                        Vars.net.disconnect();
                        Vars.player=null;
                        Groups.clear();
                    }, 3);
                }, 5);
            } catch (Exception err) {
                errorLogger.logErr(err);
            }
        });*/
        KbotCommands.Companion.KregisterCommands();
    }
}
