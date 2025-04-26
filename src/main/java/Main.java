package main.java;

import arc.*;
import arc.mock.MockFiles;
import arc.struct.Seq;
import arc.util.Log;
import main.java.bot.botLoader;
import main.java.bot.errorLogger;
import mindustry.Vars;
import mindustry.core.*;
import mindustry.game.Rules;
import mindustry.gen.Groups;
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
        Core.settings = new Settings();
        Core.files = new MockFiles();
        Vars.content = new ContentLoader();
        Vars.content.createBaseContent();
        Vars.content.loadColors();
        Vars.state = new GameState();
        Vars.state.set(GameState.State.playing);
        Vars.state.map = null;
        Vars.state.rules = new Rules();
        Vars.logic = new Logic();
        Vars.world = new World();
        Vars.netClient = new NetClient();
        Groups.init();
        loadNet();
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
                try {
                    runnable.run();
                } catch (Exception e) {
                    Vars.net.showError(e);
                }
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
