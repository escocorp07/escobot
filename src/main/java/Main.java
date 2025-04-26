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
import mindustry.core.ContentLoader;
import mindustry.core.GameState;
import mindustry.core.Platform;
import mindustry.game.Rules;
import mindustry.net.Net;
import reactor.util.Loggers;

import static main.java.ConfigLoader.loadcfg;
import static main.java.bot.utils.*;

public class Main {
    public static void main(String[] args) {
        Vars.loadLogger();
        Loggers.useCustomLoggers(new LoggerProvider());
        Log.info("Loading bot...");
        loadcfg();
        loadSettings();
        Vars.platform = new Platform() {};
        Vars.net = new BNet(Vars.platform.getNet());
        Vars.content = new ContentLoader();
        Vars.content.createBaseContent();
        Vars.state = new GameState();
        Vars.state.set(GameState.State.playing);
        Vars.state.map = null;
        Vars.state.rules = new Rules();
        Log.info("GameState initialized.");
        Core.app = new Application() {
            @Override
            public Seq<ApplicationListener> getListeners(){
                return new Seq<ApplicationListener>();
            }

            @Override
            public ApplicationType getType() {
                Log.info("GetType used");
                return null;
            }

            @Override
            public String getClipboardText() {
                Log.info("getCLTestUsed used");
                return "";
            }

            @Override
            public void setClipboardText(String s) {
                Log.info("setClText used, text @", s);
            }

            @Override
            public void post(Runnable runnable){
                //Threads.daemon(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Vars.net.showError(e);
                }
                //});
            }

            @Override
            public void exit() {
                Log.info("Exit used");
                System.exit(0);
            }
        };
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
            saveSettings();
        }));
        botLoader.load();
    }
}
