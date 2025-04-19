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
        File logDir = new File("./logs/log.txt");
        if (!logDir.exists()) logDir.mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(logDir, true);
            PrintStream psFile = new PrintStream(fos);
            PrintStream psConsole = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    psConsole.write(b);
                    psFile.write(b);
                }
            }));
        } catch (Exception e) {
            Log.warn("No logging to file!");
            errorLogger.logErr(e);
        }
        botLoader.load();
    }
}
