package main.java;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;


public class BVars {
    public static DiscordClient client;
    public static GatewayDiscordClient gateway;
    public static String btoken, prefix;
    public static Snowflake guild; // сервер бота
    /**
     * ownerid-айди роли-владельца сервера
     * reactionMessage-айди куда ставят эмодзи чтобы получить пинг роль новостей
     * newsid-роль новостей из реакшенмесадж
     * grelid-роль грелы
     * forumBannedid-не может писать в форуме заявок на админку
     * arrivalsid-место где идут оповещения о прибытии участников на сервер
     * sugid - канал для предложений участников
     * */
    public static long ownerid, reactionMessage, newsid, grelyid, forumBannedid, arrivalsid, sugid;
    public static boolean debug;
}
