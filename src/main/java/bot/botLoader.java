package main.java.bot;
// discord4j

import arc.util.Log;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import main.java.bot.commands.botCommands;
import reactor.core.publisher.Mono;

import main.java.BVars;

import java.util.Optional;

import static main.java.BVars.btoken;
import static main.java.bot.commands.commandHandler.handleEvent;

public class botLoader {
    public static void load() {
        DiscordClient client = DiscordClient.create(btoken);
        Log.info("Bot loaded!");
        BVars.login = client.withGateway(gw -> {
            botCommands.registerCommands();
            Log.info("Gateway connected!");
            BVars.gateway = gw;
            return gw.on(MessageCreateEvent.class, event -> {
                handleEvent(event);
                return Mono.empty();
            });
        });
        BVars.login.block();
    }
}