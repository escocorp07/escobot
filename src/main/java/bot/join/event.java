package main.java.bot.join;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import main.java.bot.errorLogger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static main.java.BVars.*;
import static main.java.bot.botUtils.*;

public class event {
    public static void handleJEvent(MemberJoinEvent event) {
        if(event.getGuildId()==guild) {
            if (event.getMember().getId().getTimestamp().isAfter(Instant.now().minus(30, ChronoUnit.DAYS))) {
                event.getMember().addRole(Snowflake.of(forumBannedid), "New account").subscribe();
                sendMessageP(arrivalsid, "<@" + event.getMember().getId() + ">" + " Ваш аккаунт создан недавно, вы будете автоматически зачислены в список заблокированных к подаче заявок на администратора.");
            } else {
                errorLogger.debug("acc>month");
            }
        } else {
            errorLogger.debug("Wrong guild.");
        }
    }
}
