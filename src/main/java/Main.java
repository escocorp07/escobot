package main.java;

import arc.util.Log;
import main.java.bot.botLoader;
import mindustry.Vars;

import static main.java.ConfigLoader.loadcfg;

public class Main {
    public static void main(String[] args) {
        Vars.loadLogger();
        Log.info("Loading bot...");
        loadcfg();
        botLoader.load();
    }
}
