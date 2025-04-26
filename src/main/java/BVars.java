package main.java;

import arc.struct.Seq;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class BVars {
    public static DiscordClient client;
    public static GatewayDiscordClient gateway;
    public static String btoken, prefix, joinMessage;
    public static Snowflake guild; // сервер бота
    public static long handledCommands, handledMessages;
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
    /**
     * scheduler.scheduleAtFixedRate(runnable, 0, 7, TimeUnit.SECONDS);
     * */
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static Seq<String> bannedErrs = Seq.with("ui", "TextFormatter", "renderer", "reading entity", "enableEffects", "entity", "mindustry.gen.LogicExplosionCallPacket.handled(LogicExplosionCallPacket.java:54)", "EOFException");
}
