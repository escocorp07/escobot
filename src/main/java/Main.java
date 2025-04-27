package main.java;

import arc.*;
import arc.mock.MockFiles;
import arc.struct.Seq;
import arc.util.Log;
import main.java.bot.botLoader;
import main.java.bot.errorLogger;
import mindustry.Vars;
import mindustry.core.*;
import mindustry.ctype.Content;
import mindustry.game.Rules;
import mindustry.gen.Groups;
import mindustry.mod.Mods;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import reactor.util.Loggers;

import static main.java.BVars.*;
import static main.java.ConfigLoader.loadcfg;
import static main.java.bot.utils.*;

public class Main {
    public static void main(String[] args) {
        Vars.loadLogger();
        Loggers.useCustomLoggers(new LoggerProvider());
        Log.info("Loading bot...");
        loadcfg();
        Vars.platform = new Platform() {};
        Vars.net = new BNet(Vars.platform.getNet());
        Core.files = new MockFiles();
        Core.settings = new Settings();
        Core.settings.setAppName("escobot");
        Core.settings.setDataDirectory(Core.files.local("config"));
        Core.settings.load();
        loadSettings();
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
        Vars.mods = new Mods();
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
                case "-d4jd":
                case "-discord4jdebug":
                    BVars.d4jdebug=true;
                    break;
                default:
                    Log.warn("Unkown arg @", arg);
                    break;
            }
        }
        for(Item i : Vars.content.items()) {
            if(!i.emoji().isEmpty())
                emToName.add(new emojiToName(i.emoji(), i));
        }
        for(Block i : Vars.content.blocks()) {
            if(!i.emoji().isEmpty())
                emToName.add(new emojiToName(i.emoji(), i));
        }
        for(UnitType i : Vars.content.units()) {
            if(!i.emoji().isEmpty())
                emToName.add(new emojiToName(i.emoji(), i));
        }
        errorLogger.debug("Bot running in debug mode!");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Log.info("Saving settings, please wait.");
            saveSettings();
            Core.settings.forceSave();
        }));
        botLoader.load();
    }
}
