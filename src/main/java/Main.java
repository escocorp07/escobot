package main.java;

import arc.*;
import arc.mock.MockFiles;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import main.java.bot.botLoader;
import main.java.bot.errorLogger;
import main.java.site.SiteLoader;
import mindustry.Vars;
import mindustry.core.*;
import mindustry.maps.Map;
import mindustry.mod.Mods;
import reactor.util.Loggers;

import static main.java.BVars.mainThread;
import static main.java.ConfigLoader.loadcfg;
import static main.java.utils.*;

public class Main {
    public static void main(String[] args) {
        Vars.loadLogger();
        Loggers.useCustomLoggers(new LoggerProvider());
        Log.info("Loading bot...");
        loadcfg();
        Core.files = new MockFiles();
        Core.settings = new Settings();
        Core.settings.setAppName("escobot");
        Core.settings.setDataDirectory(Core.files.local("config"));
        Core.settings.load();
        loadSettings();
        Vars.content = new ContentLoader();
        Vars.content.createBaseContent();
        Vars.content.loadColors();
        Vars.mods = new Mods();
        Vars.dataDirectory=Core.settings.getDataDirectory().child("data");
        Vars.customMapDirectory=Vars.dataDirectory.child("maps");
        Vars.emptyMap=new Map(new StringMap());
        Vars.state=new GameState();
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
            public synchronized void post(Runnable runnable){
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
                case "-d4jd":
                case "-discord4jdebug":
                    BVars.d4jdebug=true;
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
            Core.settings.forceSave();
        }));
        SiteLoader.load();
        botLoader.load();
    }
}
