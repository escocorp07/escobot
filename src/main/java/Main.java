package main.java;

import arc.util.Log;
import main.java.bot.botLoader;
import main.java.bot.errorLogger;
import mindustry.Vars;

import static main.java.ConfigLoader.loadcfg;

public class Main {
    public static void main(String[] args) {
        Vars.loadLogger();
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
    }
}
