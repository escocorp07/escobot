package main.java;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;


public class BVars {
    public static Mono<Void> login;
    public static GatewayDiscordClient gateway;
    public static String btoken, prefix;
    public static Snowflake guild;
    public static long ownerid, reactionMessage, newsid;
    public static boolean debug;
}
