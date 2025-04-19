package main.java;

import arc.util.Log;
import main.java.bot.botLoader;
import main.java.bot.errorLogger;
import mindustry.Vars;

import java.io.*;

import static main.java.ConfigLoader.loadcfg;

public class Main {
    public static void main(String[] args) {
        Vars.loadLogger();
        Log.info("Loading bot...");
        loadcfg();
        errorLogger.debug("Bot running in debug mode!");
        File logDir = new File("./logs");
        if (!logDir.exists()) logDir.mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream("./logs/log.txt", true);
            PrintStream ps = new PrintStream(fos);
            System.setOut(ps);
            System.setErr(ps);
        } catch (Exception e) {
            Log.warn("No logging to file!");
            errorLogger.logErr(e);
        }
        botLoader.load();
    }
}
