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
        botLoader.load();
    }
}
