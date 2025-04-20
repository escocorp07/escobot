package main.java.bot;

import discord4j.common.util.Snowflake;

import static main.java.BVars.gateway;

public class botUtils {
    /*Разновидности отправки сообщений*/
    public static void sendMessage(Snowflake cha, String content) {
        gateway.getChannelById(cha)
                .flatMap(ch -> ch.getRestChannel().createMessage(content.replace("@", "")))
                .subscribe();
    }
    public static void sendMessage(String cha, String content) {
        gateway.getChannelById(Snowflake.of(cha))
                .flatMap(ch -> ch.getRestChannel().createMessage(content.replace("@", "")))
                .subscribe();
    }
    public static void sendMessage(long cha, String content) {
        gateway.getChannelById(Snowflake.of(cha))
                .flatMap(ch -> ch.getRestChannel().createMessage(content.replace("@", "")))
                .subscribe();
    }
    public static void sendMessageP(Snowflake cha, String content) {
        gateway.getChannelById(cha)
                .flatMap(ch -> ch.getRestChannel().createMessage(content))
                .subscribe();
    }
    public static void sendMessageP(String cha, String content) {
        gateway.getChannelById(Snowflake.of(cha))
                .flatMap(ch -> ch.getRestChannel().createMessage(content))
                .subscribe();
    }
    public static void sendMessageP(long cha, String content) {
        gateway.getChannelById(Snowflake.of(cha))
                .flatMap(ch -> ch.getRestChannel().createMessage(content))
                .subscribe();
    }
}
