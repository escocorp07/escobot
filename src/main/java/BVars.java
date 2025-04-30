package main.java;

import arc.struct.Seq;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import main.java.bot.utils;
import mindustry.type.Item;

import javax.script.ScriptEngine;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BVars {
    public static DiscordClient client;
    public static GatewayDiscordClient gateway;
    public static String btoken, prefix, joinMessage;
    public static Snowflake guild; // сервер бота
    public static long handledCommands, handledMessages;
    public static ScriptEngine ktsEngine = null;
    public static Seq<Snowflake> bannedInSug = new Seq<>();
    /**
     * ownerid-айди роли-владельца сервера
     * reactionMessage-айди куда ставят эмодзи чтобы получить пинг роль новостей
     * newsid-роль новостей из реакшенмесадж
     * grelid-роль грелы
     * forumBannedid-не может писать в форуме заявок на админку
     * arrivalsid-место, где идут оповещения о прибытии участников на сервер
     * sugid - канал для предложений участников
     * sugping - сообщение для получения роли пинга
     * sugpingrole - роль пинга
     * */
    public static long ownerid, reactionMessage, newsid, grelyid, forumBannedid, arrivalsid, sugid, sugping, sugpingrole;
    public static boolean debug, d4jdebug;
    public static String DB_USER;
    public static String DB_PASSWORD;
    public static Seq<String> bannedErrs = Seq.with("ui", "TextFormatter", "renderer", "reading entity", "enableEffects", "entity", "mindustry.gen.LogicExplosionCallPacket.handled(LogicExplosionCallPacket.java:54)", "EOFException");

    public static Random random = new Random();

    public static Seq<String> yesDialogs = Seq.with("✅ Уверен в этом!", "\uD83D\uDC4D Отличная идея!", "\uD83D\uDC4C Звучит хорошо.");
    public static Seq<String> noDialogs = Seq.with("❌ Плохая идея", "\uD83D\uDE35 Даже не думай об этом!", "\uD83D\uDC4E Не думаю, что это кончится хорошо.");
    public static Seq<String> idkDialogs = Seq.with("❓ Я не знаю!", "☁ Я не уверен.", "\uD83D\uDD2E Спроси позже.", "Я Сейчас не в духе, извини.", "Не могу сказать.");
}
