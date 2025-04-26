package main.java.bot.emoji;

import static main.java.bot.emoji.emojiHandler.*;

import arc.util.Log;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;
import static main.java.BVars.*;

public class botEmoji {
    /**Зарегестрировать эмодзи на опр. сообщении -> код*/
    public static void registerEmojis() {
        Log.info("Time to create emojis!");
            registerEmojiAdd("✡\uFE0F", Snowflake.of(sugping), e->{
                e.getUser().flatMap(u->{
                    u.asMember(guild).flatMap(m->{
                        m.addRole(Snowflake.of(sugpingrole), "Reaction-role.").subscribe();
                        return Mono.empty();
                    }).subscribe();
                    return Mono.empty();
                }).subscribe();
            });
            registerEmojiRemove("✡\uFE0F", Snowflake.of(sugping), e->{
                e.getUser().flatMap(u->{
                    u.asMember(guild).flatMap(m->{
                        m.removeRole(Snowflake.of(sugpingrole), "Reaction-role.").subscribe();
                        return Mono.empty();
                    }).subscribe();
                    return Mono.empty();
                }).subscribe();
            });
    }
}