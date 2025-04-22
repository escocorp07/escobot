package main.java;

import arc.*;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.OS;
import arc.util.Threads;
import arc.util.Timer;
import main.java.bot.botLoader;
import main.java.bot.errorLogger;
import mindustry.Vars;
import reactor.util.Loggers;

import static main.java.ConfigLoader.loadcfg;

public class Main {
    public static void main(String[] args) {
        Vars.loadLogger();
        Loggers.useCustomLoggers(new LoggerProvider());
        Log.info("Loading bot...");
        loadcfg();
        for (String arg : args) {
            if (arg.equals("-d")) {
                BVars.debug = true;
                break;
            }
        }
        errorLogger.debug("Bot running in debug mode!");
        botLoader.load();
        Log.info("Bot exited?");
    }
}
