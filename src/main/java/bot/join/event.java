package main.java.bot.join;

import arc.util.Log;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import main.java.bot.errorLogger;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static main.java.BVars.*;
import static main.java.bot.botUtils.*;

public class event {
    public static void handleJEvent(MemberJoinEvent event) {
        errorLogger.debug("Event received to func., your @ rec @", guild, event.getGuildId());
        if(event.getGuildId().equals(guild)) {
            if (event.getMember().getId().getTimestamp().isAfter(Instant.now().minus(30, ChronoUnit.DAYS))) {
                errorLogger.debug("Giving role.");
                event.getMember().addRole(Snowflake.of(forumBannedid), "New account").subscribe();
                sendMessageP(arrivalsid, "<@" + event.getMember().getId().asString() + ">" + " Ваш аккаунт создан недавно, вы будете автоматически зачислены в список заблокированных к подаче заявок на администратора.");
            } else {
                errorLogger.debug("acc>month");
            }
            event.getMember().getPrivateChannel().flatMap(ch-> {
                        ch.createMessage("Короткий список часто задаваемых вопросов, пожалуйста, не задавайте вопросы пингуя глав сервера не прочитав данный список.\nГде подать заявку на администратора?\nЕсть специальный канал \"applications\", туда вы можете подать заявку.Для получения доступа привяжите игровой аккаунт к аккаунту дискорда с помощью игровой команды /link\nГде подать аппеляцию?\nНайдите канал \"appeals\", там есть форма подачи заявки.\nГде пожаловаться на игрока?\nНайдите канал \"reports\" и напишите жалобу по закрепленной форме.\nВладелец сервера - grelylrz\nСо-Владелец - noname0302\n**НЕ ПИНГУЙТЕ ИХ БЕЗ НАДОБНОСТИ**").subscribe();
                        return Mono.empty();
                    }
            ).subscribe();
        } else {
            errorLogger.debug("Wrong guild.");
        }
    }
}
