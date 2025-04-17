package main.kotlin.bot

import discord4j.core.event.domain.message.MessageCreateEvent
import main.java.bot.botUtils.sendMessage
import main.java.bot.commands.commandHandler.*

class KbotCommands {
    companion object {
        fun KregisterCommands() {
            registerCommand("help", "See command list") { ev: MessageCreateEvent, args: Array<String?> ->
                val sb: StringBuilder = StringBuilder()
                commands.each() { c: botcommand ->
                    sb.append("- "+c.name+" - "+c.description+"\n")
                }
                sendMessage(ev.message.channelId, sb.toString())
                sb.setLength(0)
            }
        }
    }
}