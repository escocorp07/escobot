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
    public static void sendMessage(int cha, String content) {
        gateway.getChannelById(Snowflake.of(cha))
                .flatMap(ch -> ch.getRestChannel().createMessage(content.replace("@", "")))
                .subscribe();
    }
}
