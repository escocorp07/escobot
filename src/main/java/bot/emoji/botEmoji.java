package main.java.bot.emoji;

import static main.java.bot.emoji.emojiHandler.*;

import arc.util.Log;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;
import static main.java.BVars.*;

public class botEmoji {
    /**Зарегестрировать эмодзи на опр. сообщении -> код*/
    public static void registerEmojis() {
        if(debug) {
            registerEmojiAdd("\uD83D\uDED0", Snowflake.of(1363081927690686505L), e -> {
                Log.info("EventA!");
            });
            registerEmojiRemove("\uD83D\uDED0", Snowflake.of(1363081927690686505L), e -> {
                Log.info("EventR!");
            });
        }
    }
}