package main.java.bot.join;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.spec.BanQuerySpec;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static main.java.BVars.*;
import static main.java.bot.botUtils.sendMessageP;

public class event {
    public static void handleJEvent(MemberJoinEvent event) {
        if(event.getGuildId().equals(guild)) {
            if (event.getMember().getId().getTimestamp().isAfter(Instant.now().minus(30, ChronoUnit.DAYS))) {
                if (noTwinkMode) {
                    event.getMember().getPrivateChannel().flatMap(u->{
                        u.createMessage("Включен режим защиты от твинков, вам вход запрещен, обжаловать можно написав noname0302 или grelylrz(пишите сразу обоим).").subscribe();
                        return Mono.empty();
                    }).subscribe();
                    event.getMember().ban(BanQuerySpec.builder().reason("No twink mode enabled").build());
                } else {
                    event.getMember().addRole(Snowflake.of(forumBannedid), "New account").subscribe();
                    sendMessageP(arrivalsid, "<@" + event.getMember().getId().asString() + ">" + " Ваш аккаунт создан недавно, вы будете автоматически зачислены в список заблокированных к подаче заявок на администратора.");
                }
            }
            event.getMember().getPrivateChannel().flatMap(u->{
                u.createMessage(joinMessage.replace("<@ping>", "<@"+event.getMember().getId().asString()+">")).subscribe();
                return Mono.empty();
            }).subscribe();
        }
    }
}
