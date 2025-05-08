package main.java.bot.commands;

import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.OS;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildMessageChannel;
//import discord4j.core.object.reaction.ReactionEmoji; // deprecated
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.StartThreadFromMessageSpec;
import discord4j.rest.util.Color;
import main.java.bot.errorLogger;
import main.kotlin.bot.KbotCommands;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static main.java.BVars.*;
import static main.java.Database.DatabaseConnector.*;
import static main.java.bot.botUtils.*;
import static main.java.bot.commands.commandHandler.commands;
import static main.java.bot.commands.commandHandler.*;
import static main.java.BuildInfo.*;
import static main.java.utils.*;
import static mindustry.Vars.maps;

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
            sb.append("Requests handled: "+getReqHandled()+"\n");
            sendMessage(e.getMessage().getChannelId(), sb.toString());
            sb.setLength(0);
        });
        registerCommand("say", "Сказать от имени бота.", "<text...>", ownerid, (e, args)->{
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg + " ");
            }
            sendMessage(e.getMessage().getChannelId(), sb.toString());
            sb.setLength(0);
        });
        registerCommand("unban", "Разбанить игрока по айди бана или юид.", "<ban-uuid or id>", admin_id, (e, args)->{
            try {
                if(unbanPlayerByBanId(Integer.parseInt(args[0])))
                    sendReply(e.getMessage(), "Unbanned.");
                else
                    sendReply(e.getMessage(), "**NOT** unbanned.");
            } catch (NumberFormatException ex) {
                if(unbanPlayerByUUID(args[0]))
                    sendReply(e.getMessage(), "Unbanned.");
                else
                    sendReply(e.getMessage(), "**NOT** unbanned.");
            }
        });
        registerCommand("test", "Test command", "[text...]", ownerid, (e, args)->{
            Vars.world.setGenerating(true);
            Vars.world.loadMap(maps.loadInternalMap("groundZero"));
            Vars.world.resize(512, 512);
            Seq<Floor> floors = new Seq<>();
            for(Block b : Vars.content.blocks()) {
                if(b.isFloor() && !b.name.contains("air"))
                    floors.add((Floor) b);
            }
            for(int y=1;y<512;y+=1) {
                for(int x=1;x<512;x+=1) {
                    Vars.world.tile(x, y).setFloor(floors.get(random.nextInt(floors.size)));
                }
            }
            Vars.world.setGenerating(false);
        }).setVisible(false);
        registerCommand("js", "js really", "<text...>", ownerid, (e, args)->{
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
        registerCommand("ball", "Погонять шары", "<text...>", (e, args) -> {
            if (args.length < 1) {
                sendReply(e.getMessage(), "Сообщение не содержит аргсов.");
                return;
            }

            Color color = Color.GRAY;
            String reply = "";
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg + " ");
            }

            int rand = random.nextInt(100);

            if (rand < 45) {
                reply = yesDialogs.get(random.nextInt(yesDialogs.size));
                color = Color.GREEN;
            } else if (rand < 90) {
                reply = noDialogs.get(random.nextInt(noDialogs.size));
                color = Color.RED;
            } else {
                reply = idkDialogs.get(random.nextInt(idkDialogs.size));
                color = Color.BLUE;
            }

            sb.setLength(255);
            sendEmbedReply(EmbedCreateSpec.builder().title(sb.toString()).addField("", reply, false).color(color).build(), e.getMessage());
            sb.setLength(0);
        });
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
        registerCommand("suggest-ban", "Запретить предложку", "<snowflakeid>", ownerid, (e, args)->{
            try {
                for(String arg : args) {
                    bannedInSug.add(Snowflake.of(Long.parseLong(arg.replace("@", "").replace("<", "").replace(">", "").trim())));
                }
            } catch (Exception err) {
                sendMessage(e.getMessage().getChannelId(), "Не Snowflake!");
            }
        });
        registerCommand("suggest-unban", "Разрешить предложку", "<snowflakeid>", ownerid, (e, args)->{
            try {
                for(String arg : args) {
                    bannedInSug.remove(Snowflake.of(Long.parseLong(arg.replace("@", "").replace("<", "").replace(">", "").trim())));
                }
            } catch (Exception err) {
                sendMessage(e.getMessage().getChannelId(), "Не Snowflake!");
            }
        });
        registerCommand("suggest-banned", "Список людей с запретом предложки", (e, args)->{
            StringBuilder sb = new StringBuilder();
            for(Snowflake f : bannedInSug) {
                sb.append("<@" + f.asString() + ">\n");
            }
            if(sb.length() > 1023)
                sb.setLength(1023);
            sendEmbedReply(EmbedCreateSpec.builder().addField("Плохиши.", sb.toString(), false).color(Color.BISMARK).build(), e.getMessage());
            sb.setLength(0);
        });
        registerCommand("suggest", "Предложить идею", "<text...>", (e, args)->{
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
                        m.addReaction(Emoji.unicode("✅")).subscribe();
                        m.addReaction(Emoji.unicode("❌")).subscribe();
                        m.startThread(StartThreadFromMessageSpec.builder().name("Обсуждение").reason("Suggest created").build()).subscribe(t->{
                            t.createMessage("Ветка создана.");
                            return;
                        });
                    });
            sb.setLength(0);
        });
        registerCommand("do", "А тебя это не должно волновать", "<code...>", grelyid, (e, args) -> {
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
        }).setVisible(false);
        registerCommand("help", "Посмотреть команды и их описания", "[command]", (e, args) -> {
            if(args.length<1) {
                EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder()
                        .color(Color.GREEN);
                for (botcommand c : commands) {
                    if(c.isVisible())
                        embed.addField(c.getName()+" "+c.getArgsN(), c.getDescription(), false);
                }
                MessageCreateSpec.Builder ms = MessageCreateSpec.builder()
                        .addEmbed(embed.build());
                gateway.getChannelById(e.getMessage().getChannelId())
                        .ofType(GuildMessageChannel.class)
                        .flatMap(channel -> channel.createMessage(ms.build()
                        )).subscribe();
            } else {
                botcommand command = commands.find(c->{
                        return c.getName().equals(args[0]) && c.isVisible();
                });
                if(command==null){
                    sendReply(e.getMessage(), "Команда не найдена!");
                }
                EmbedCreateSpec.Builder b = EmbedCreateSpec.builder().title(command.getName()).addField("", command.getDescription(), false)
                                .color(Color.GREEN);
                if(!command.getArgsN().isEmpty())
                    b.description(prefix+command.getName()+" "+command.getArgsN());
                sendEmbedReply(b.build(), e.getMessage());
            }
        });
        registerCommand("set-join", "Установить сообщение отправляемое участнику по заходу.", "<text...>", grelyid, (e, args) -> {
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
        }).setVisible(false);
        registerCommand("set-status", "Установить статус бота", "<text...>", grelyid, (e, args) -> {
            if(args.length == 0) {
                sendMessage(e.getMessage().getChannelId(), "Статус убран.");
                presence="";
                gateway.updatePresence(ClientPresence.doNotDisturb(ClientActivity.playing("Думай."))).subscribe();
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg + " ");
            }
            sb.setLength(128);
            presence=sb.toString();
            gateway.updatePresence(ClientPresence.doNotDisturb(ClientActivity.playing(sb.toString()))).subscribe();
            sendReply(e.getMessage(), "Ok.");
            sb.setLength(0);
        }).setVisible(false);
        registerCommand("pstats", "Посмотреть стату игрока", "<id>", (e, args)->{
            if(args.length<1) {
                sendReply(e.getMessage(), "Wrong id!");
                return;
            }
            int id;
            try {
                id = Integer.parseInt(args[0]);
            } catch (NumberFormatException lox) {
                sendReply(e.getMessage(), "Wrong id!");
                return;
            }
            getPlayerById(id).ifPresent(d->{
                sendReply(e.getMessage(), "Level: "+d.getLevel()
                +"\nEXP: "+d.getExperience()
                +"\nWins: "+d.getWins()
                +"\nLoses: "+d.getLoses()
                +"\nBlocks placed/broken: "+d.getBlocksPlaced()+"/"+d.getBlocksBroken()
                +"\nWaves survived: "+d.getWavesSurvived()
                +"\nPlaytime: " + d.getPlaytime() / (7 * 24 * 3600) + "w " + (d.getPlaytime() % (7 * 24 * 3600)) / (24 * 3600) + "d " + (d.getPlaytime() % (24 * 3600)) / 3600 + "h " + (d.getPlaytime() % 3600) / 60 + "m " + d.getPlaytime() % 60 + "s");
            });
        });
        KbotCommands.Companion.KregisterCommands();
        generateCommands();
        registerSQLCommands();
    }
}
