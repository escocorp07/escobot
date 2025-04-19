package main.java.bot.emoji;

import static main.java.bot.emoji.emojiHandler.*;

import arc.util.Log;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;

public class botEmoji {
    public static void registerEmojis() {
        registerEmojiAdd("\uD83D\uDED0", Snowflake.of(1363081927690686505L), e -> {
            Log.info("EventA!");
            e.getUser().flatMap(u->{
                Log.info(u.getUsername());
                return Mono.empty();
            });
        });
        registerEmojiRemove("\uD83D\uDED0", Snowflake.of(1363081927690686505L), e -> {
            Log.info("EventR!");
            e.getUser().flatMap(u->{
                Log.info(u.getUsername());
                return Mono.empty();
            });
        });
    }
}