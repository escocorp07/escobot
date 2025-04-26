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
            }
            switch (arg) {
                case "-d":
                case "--debug":
                    BVars.debug = true;
                    break;
                default:
                    Log.warn("Unkown arg @", arg);
                    break;
            }
        }
        errorLogger.debug("Bot running in debug mode!");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Log.info("Saving settings, please wait.");
            // TODO
        }));
        botLoader.load();
    }
}
