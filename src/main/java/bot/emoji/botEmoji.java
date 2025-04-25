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
            registerEmojiAdd("✡", Snowflake.of(1365376732600860713L), e->{
                e.getUser().flatMap(u->{
                    u.asMember(guild).flatMap(m->{
                        m.addRole(Snowflake.of(1365377730115403837L), "Reaction-role.").subscribe();
                        return Mono.empty();
                    }).subscribe();
                    return Mono.empty();
                }).subscribe();
            });
            registerEmojiRemove("✡", Snowflake.of(1365376732600860713L), e->{
                e.getUser().flatMap(u->{
                    u.asMember(guild).flatMap(m->{
                        m.removeRole(Snowflake.of(1365377730115403837L), "Reaction-role.").subscribe();
                        return Mono.empty();
                    }).subscribe();
                    return Mono.empty();
                }).subscribe();
            });
        }
    }
}