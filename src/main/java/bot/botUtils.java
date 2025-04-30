package main.java.bot;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.object.entity.channel.*;
import discord4j.discordjson.json.MessageReferenceData;
import reactor.core.publisher.Mono;

import static main.java.BVars.gateway;

public class botUtils {
    /*Разновидности отправки сообщений*/
    public static void sendMessage(Snowflake cha, String content) {
        gateway.getChannelById(cha)
                .flatMap(ch -> ch.getRestChannel().createMessage(content.replace("@", "")))
                .subscribe();
    }
    public static void sendMessage(String cha, String content) {
        sendMessage(Snowflake.of(cha), content);
    }
    public static void sendMessage(long cha, String content) {
        sendMessage(Snowflake.of(cha), content);
    }
    public static void sendMessageP(Snowflake cha, String content) {
        gateway.getChannelById(cha)
                .flatMap(ch -> ch.getRestChannel().createMessage(content))
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
                    ch.createMessage(m).subscribe();
                    return Mono.empty();
        }).subscribe();
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
                    ch.createMessage(MessageCreateSpec.builder().addEmbed(e).build()).subscribe();
                    return Mono.empty();
                }).subscribe();
    }
    public static void sendEmbed(String cha, EmbedCreateSpec e){
        sendEmbed(Snowflake.of(cha), e);
    }
    public static void sendEmbed(Long cha, EmbedCreateSpec e){
        sendEmbed(Snowflake.of(cha), e);
    }
    /*Ответить на сообщение*/
    public static void sendReply(Message msg, String content, boolean ping) {
        sendMessage(msg.getChannelId(), MessageCreateSpec.builder().messageReference(MessageReferenceData.builder().channelId(msg.getChannelId().asLong()).messageId(msg.getId().asLong()).build()).content(content).build());
    }
    /*Ответ с эмбедом*/

    public static void sendEmbedReply(EmbedCreateSpec e, Message msg) {
        msg.getChannel()
                .flatMap(ch -> {
                    ch.createMessage(MessageCreateSpec.builder().addEmbed(e).messageReference(MessageReferenceData.builder().channelId(msg.getChannelId().asLong()).messageId(msg.getId().asLong()).build()).build()).subscribe();
                    return Mono.empty();
                }).subscribe();
    }
}
