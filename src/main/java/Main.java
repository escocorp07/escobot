package main.java;

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
        Timer.schedule(() -> {
            Threads.daemon("autoUpdate", () -> {
                String out = OS.exec("git", "pull").trim().toLowerCase();
                if (!out.contains("already up to date")) {
                    Log.info("Auto updating! "+BuildInfo.GIT_HASH+" -> "+OS.exec("git", "rev-parse --short HEAD"));
                    System.exit(92148); // autoupd code.
                } else {
                    Log.info("No new updates found!");
                }
            });
        }, 0, 30 * 60);
        botLoader.load();
    }
}
