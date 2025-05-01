package main.java.bot.commands;

import arc.struct.Seq;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
//import discord4j.core.object.reaction.ReactionEmoji; // deprecated
import discord4j.core.object.emoji.Emoji;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.MessageReferenceData;
import lombok.Getter;
import lombok.Setter;
import main.java.BVars;
import main.java.bot.errorLogger;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;

import static main.java.BVars.*;
import static main.java.bot.botUtils.sendMessage;

/**Сделанный мною хендлер команд бота.*/
public class commandHandler {
    public static Seq<botcommand> commands = new Seq<>();
    /**Зарегестрировать обычную команду.*/
    public static botcommand registerCommand(String name, String description, BiConsumer<MessageCreateEvent, String[]> executor) {
        botcommand c = new botcommand(name, description, executor);
        commands.add(c);
        return c;
    }
    /**Зарегестрировать команду с требованием к доступу.*/
    public static botcommand registerCommand(String name, String description, long role, BiConsumer<MessageCreateEvent, String[]> executor) {
        botcommand c = new botcommand(name, description, executor);
        c.setRoleID(role);
        commands.add(c);
        return c;
    }
    /*обычная+аргсы*/
    public static botcommand registerCommand(String name, String description, String argsN, BiConsumer<MessageCreateEvent, String[]> executor) {
        botcommand c = new botcommand(name, description, executor);
        c.setArgsN(argsN);
        commands.add(c);
        return c;
    }
    /**доступ+аргсы*/
    public static botcommand registerCommand(String name, String description, String argsN, long role, BiConsumer<MessageCreateEvent, String[]> executor) {
        botcommand c = new botcommand(name, description, executor);
        c.setRoleID(role);
        c.setArgsN(argsN);
        commands.add(c);
        return c;
    }
    /**Обработать эвент получения сообщения.*/
    public static void handleEvent(MessageCreateEvent event) {
        Message message = event.getMessage();
        Optional<User> authorOpt = message.getAuthor();
        if (authorOpt.isPresent() && authorOpt.get().isBot()) {
            return;
        }
        User author = authorOpt.get();
        String content = message.getContent();
        if(content.startsWith(BVars.prefix)) {
            String[] args = content.replace(BVars.prefix, "").trim().split(" ");
            botcommand command = commands.find(c->{
                return c.getName().equals(args[0]);
            });
            if(command != null) {
                handledCommands+=1;
                // Arrays.copyOfRange(args, 1, args.length)
                if(command.getRoleID() == 0) {
                    command.exec(event, Arrays.copyOfRange(args, 1, args.length));
                } else {
                    try {
                        author.asMember(BVars.guild).flatMap(m -> {
                            if (m.getRoleIds().contains(Snowflake.of(command.getRoleID())) || command.getRoleID() == 0) {
                                command.exec(event, Arrays.copyOfRange(args, 1, args.length));
                            } else {
                                //sendMessage(message.getChannelId(), "No access.");
                                sendMessage(message.getChannelId(), MessageCreateSpec.builder().messageReference(MessageReferenceData.builder().channelId(message.getChannelId().asLong()).messageId(message.getId().asLong()).build()).content("No access").build());
                                message.addReaction(Emoji.unicode("❌")).subscribe();
                            }
                            return Mono.empty();
                        }).subscribe();
                    } catch (Exception e) {
                        errorLogger.logErr(e);
                    }
                }
            }
        }
    }
    @Getter
    @Setter
    public static class botcommand {
        public String name;
        public String description;
        public BiConsumer<MessageCreateEvent, String[]> executor;
        public long roleID;
        public String argsN = "";

        botcommand(String name, String description, BiConsumer<MessageCreateEvent, String[]> executor) {
            this.name = name;
            this.description = description;
            this.executor = executor;
        }

        public void exec(MessageCreateEvent e, String[] args) {
            executor.accept(e, args);
        }
    }
}
