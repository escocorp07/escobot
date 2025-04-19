package main.java;

import arc.util.Log;
import main.java.bot.botLoader;
import main.java.bot.errorLogger;
import mindustry.Vars;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static main.java.ConfigLoader.loadcfg;

public class Main {
    public static void main(String[] args) {
        Vars.loadLogger();
        Log.info("Loading bot...");
        loadcfg();
        errorLogger.debug("Bot running in debug mode!");
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        File logDir = new File("./logs/log-"+date+".txt");
        if (!logDir.exists()) logDir.mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(logDir, true);
            PrintStream psFile = new PrintStream(fos);
            PrintStream psConsole = System.out;
            Log.info("Logging loaded!");
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " ";
                    String message = timestamp + String.valueOf((char) b);
                    psConsole.write(message.getBytes());
                    psFile.write(message.getBytes());
                }
            }));
        } catch (Exception e) {
            Log.warn("No logging to file!");
            errorLogger.logErr(e);
        }
        botLoader.load();
    }
}
