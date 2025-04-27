package main.java;

import arc.struct.Seq;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import javax.script.ScriptEngine;

public class BVars {
    public static DiscordClient client;
    public static GatewayDiscordClient gateway;
    public static String btoken, prefix, joinMessage;
    public static Snowflake guild; // сервер бота
    public static long handledCommands, handledMessages;
    public static ScriptEngine ktsEngine = null;
    public static Seq<Long> bannedInSug = new Seq<>();
    /**
     * ownerid-айди роли-владельца сервера
     * reactionMessage-айди куда ставят эмодзи чтобы получить пинг роль новостей
     * newsid-роль новостей из реакшенмесадж
     * grelid-роль грелы
     * forumBannedid-не может писать в форуме заявок на админку
     * arrivalsid-место где идут оповещения о прибытии участников на сервер
     * sugid - канал для предложений участников
     * sugping - сообщение для получение роли пинга
     * sugpingrole - роль пинга
     * */
    public static long ownerid, reactionMessage, newsid, grelyid, forumBannedid, arrivalsid, sugid, sugping, sugpingrole;
    public static boolean debug;
    public static Seq<String> bannedErrs = Seq.with("ui", "TextFormatter", "renderer", "reading entity", "enableEffects", "entity", "mindustry.gen.LogicExplosionCallPacket.handled(LogicExplosionCallPacket.java:54)", "EOFException");
}
