package main.java.bot.emoji;

import arc.struct.Seq;
import arc.util.Log;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import lombok.*;

import java.util.function.Consumer;

public class emojiHandler {
    public static Seq<emojiAdd> emojia = new Seq<>();
    public static Seq<emojiRemove> emojir = new Seq<>();

    public static void registerEmojiAdd(String raw, Snowflake messageId, Consumer<ReactionAddEvent> code) {
        emojia.add(new emojiAdd(raw, messageId, code));
    }

    public static void registerEmojiRemove(String raw, Snowflake messageId, Consumer<ReactionRemoveEvent> code) {
        emojir.add(new emojiRemove(raw, messageId, code));
    }

    public static void handleEmojiEvent(Object o) {
        if (o instanceof ReactionAddEvent event) {
            emojia.each(e -> {
                String emojiRaw = event.getEmoji().asUnicodeEmoji()
                        .map(unicode -> unicode.getRaw())
                        .orElse("");
                if (e.getRaw().equals(emojiRaw)
                        && e.getMessageId().equals(event.getMessageId())) {
                    e.getExec().accept(event);
                }
            });
        } else if (o instanceof ReactionRemoveEvent event) {
            emojir.each(e -> {
                String emojiRaw = event.getEmoji().asUnicodeEmoji()
                        .map(unicode -> unicode.getRaw())
                        .orElse("");
                if (e.getRaw().equals(emojiRaw)
                        && e.getMessageId().equals(event.getMessageId())) {
                    e.getExec().accept(event);
                }
            });
        } else {
            Log.warn("What do you put into event?");
        }
    }

    @Getter
    @Setter
    public static class emojiAdd {
        String raw;
        Snowflake messageId;
        Consumer<ReactionAddEvent> exec;

        public emojiAdd(String emoji, Snowflake messageId, Consumer<ReactionAddEvent> cons) {
            this.raw = emoji;
            this.messageId = messageId;
            this.exec = cons;
        }
    }

    @Getter
    @Setter
    public static class emojiRemove {
        String raw;
        Snowflake messageId;
        Consumer<ReactionRemoveEvent> exec;

        public emojiRemove(String emoji, Snowflake messageId, Consumer<ReactionRemoveEvent> cons) {
            this.raw = emoji;
            this.messageId = messageId;
            this.exec = cons;
        }
    }
}