package main.java.bot;

import arc.struct.Seq;
import arc.util.Log;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.object.entity.channel.*;
import discord4j.discordjson.json.MessageReferenceData;
import main.java.BVars;
import main.java.annotations.GenerateSet;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static main.java.BVars.gateway;
import static main.java.BVars.grelyid;
import static main.java.bot.commands.commandHandler.registerCommand;

public class botUtils {
    public static void generateCommands() {
        Log.info("Time to generate annotated commands.");
        Field[] fields = BVars.class.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(GenerateSet.class)) {
                GenerateSet annotation = field.getAnnotation(GenerateSet.class);
                String commandName = annotation.name().isEmpty() ? "set-" + field.getName() : annotation.name();

                registerCommand(commandName, "Установить " + field.getName(), "<value...>", grelyid, (e, args) -> {
                    if (args.length == 0) {
                        try {
                            Class<?> type = field.getType();
                            if (type == String.class) {
                                field.set(null, "");
                            } else if (type == int.class || type == Integer.class) {
                                field.set(null, 0);
                            } else if (type == long.class || type == Long.class) {
                                field.set(null, 0L);
                            } else if (Seq.class.isAssignableFrom(type)) {
                                field.set(null, new Seq<>());
                            }
                            sendMessage(e.getMessage().getChannelId(), "Удалено " + field.getName());
                        } catch (Exception ex) {
                            sendMessage(e.getMessage().getChannelId(), ex.getMessage());
                        }
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (String arg : args) {
                        sb.append(arg).append(" ");
                    }
                    try {
                        Class<?> type = field.getType();
                        if (type == String.class) {
                            field.set(null, sb.toString().trim());
                        } else if (type == int.class || type == Integer.class) {
                            field.set(null, Integer.parseInt(sb.toString().trim()));
                        } else if (type == long.class || type == Long.class) {
                            field.set(null, Long.parseLong(sb.toString().trim()));
                        } else if (Seq.class.isAssignableFrom(type)) {
                            Type genericType = field.getGenericType();
                            if (genericType instanceof ParameterizedType) {
                                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                                Type elementType = parameterizedType.getActualTypeArguments()[0];

                                Seq<Object> list = new Seq<>();

                                for (String arg : args) {
                                    if (elementType == String.class) {
                                        list.add(arg);
                                    } else if (elementType == Integer.class || elementType == int.class) {
                                        list.add(Integer.parseInt(arg));
                                    } else if (elementType == Long.class || elementType == long.class) {
                                        list.add(Long.parseLong(arg));
                                    } else if (elementType == Double.class || elementType == double.class) {
                                        list.add(Double.parseDouble(arg));
                                    } else if (elementType == Boolean.class || elementType == boolean.class) {
                                        list.add(Boolean.parseBoolean(arg));
                                    }
                                }

                                field.set(null, list);
                            }
                        }
                        sendMessage(e.getMessage().getChannelId(), "Назначено " + field.getName() + ": " + sb.toString().trim());
                    } catch (Exception ex) {
                        sendMessage(e.getMessage().getChannelId(), ex.getMessage());
                    }
                });
            }
        }
    }

    /*Разновидности отправки сообщений*/
    public static void sendMessage(Snowflake cha, String content) {
        if(content.isEmpty())
            return;
        gateway.getChannelById(cha)
                .flatMap(ch -> {
                    ch.getRestChannel().createMessage(content.replace("@", "")).doOnError(errorLogger::logErr).subscribe();
                    return Mono.empty();
                }).doOnError(errorLogger::logErr).subscribe();
    }
    public static void sendMessage(String cha, String content) {
        sendMessage(Snowflake.of(cha), content);
    }
    public static void sendMessage(long cha, String content) {
        sendMessage(Snowflake.of(cha), content);
    }
    public static void sendMessageP(Snowflake cha, String content) {
        if(content.isEmpty())
            return;
        gateway.getChannelById(cha)
                .flatMap(ch ->{
                    ch.getRestChannel().createMessage(content).doOnError(errorLogger::logErr).subscribe();
                    return Mono.empty();
                })
                .doOnError(errorLogger::logErr)
                .subscribe();
    }
    public static void sendMessageP(String cha, String content) {
        sendMessageP(Snowflake.of(cha), content);
    }
    public static void sendMessageP(long cha, String content) {
        sendMessageP(Snowflake.of(cha), content);
    }
    /*Отправка с MessageCreateSpec*/
    public static void sendMessage(Snowflake cha, MessageCreateSpec m) {
        gateway.getChannelById(cha)
                .ofType(MessageChannel.class)
                .flatMap(ch->{
                    ch.createMessage(m)
                            .doOnError(errorLogger::logErr).
                            subscribe();
                    return Mono.empty();
        }).doOnError(errorLogger::logErr).subscribe();
    }
    public static void sendMessage(String cha, MessageCreateSpec m){
        sendMessage(Snowflake.of(cha), m);
    }
    public static void sendMessage(Long cha, MessageCreateSpec m){
        sendMessage(Snowflake.of(cha), m);
    }
    /*Отправка эмбеда*/
    public static void sendEmbed(Snowflake cha, EmbedCreateSpec e) {
        gateway.getChannelById(cha)
                .ofType(MessageChannel.class)
                .flatMap(ch->{
                    ch.createMessage(MessageCreateSpec.builder().addEmbed(e).build()).doOnError(errorLogger::logErr).subscribe();
                    return Mono.empty();
                }).doOnError(errorLogger::logErr).subscribe();
    }
    public static void sendEmbed(String cha, EmbedCreateSpec e){
        sendEmbed(Snowflake.of(cha), e);
    }
    public static void sendEmbed(Long cha, EmbedCreateSpec e){
        sendEmbed(Snowflake.of(cha), e);
    }
    /*Ответить на сообщение*/
    public static void sendReply(Message msg, String content) {
        if(content.isEmpty())
            return;
        sendMessage(msg.getChannelId(), MessageCreateSpec.builder().messageReference(MessageReferenceData.builder().channelId(msg.getChannelId().asLong()).messageId(msg.getId().asLong()).build()).content(content).build());
    }
    /*Ответ с эмбедом*/
    public static void sendEmbedReply(EmbedCreateSpec e, Message msg) {
        msg.getChannel()
                .flatMap(ch -> {
                    ch.createMessage(MessageCreateSpec.builder().addEmbed(e).messageReference(MessageReferenceData.builder().channelId(msg.getChannelId().asLong()).messageId(msg.getId().asLong()).build()).build()).doOnError(errorLogger::logErr).subscribe();
                    return Mono.empty();
                }).doOnError(errorLogger::logErr).subscribe();
    }
}
