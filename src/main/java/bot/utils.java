package main.java.bot;

import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.util.Log;
import arc.util.serialization.*;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import lombok.Getter;
import lombok.Setter;
import main.java.BVars;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.net.Packets;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import arc.*;
import arc.files.Fi;
import arc.math.Rand;
import arc.mock.MockFiles;
import arc.net.Client;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.TaskQueue;
import arc.util.Threads;
import arc.util.Timer;
import arc.util.serialization.Base64Coder;
import mindustry.Vars;
import mindustry.core.*;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.game.Universe;
import mindustry.gen.*;
import mindustry.net.ArcNetProvider;
import mindustry.net.ArcNetProvider.PacketSerializer;
import mindustry.net.Net;
import mindustry.net.NetworkIO;
import mindustry.net.Packets;
import mindustry.net.Packets.Connect;
import mindustry.net.Packets.Disconnect;
import mindustry.net.Packets.WorldStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.zip.InflaterInputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import main.java.bot.commands.commandHandler;
import org.reflections.Reflections;
import java.lang.reflect.Field;
import static main.java.BVars.*;
import static mindustry.io.MapIO.colorFor;

public class utils {
    /**Получить рендер карт с цветом предмета в конфиге блока(сортер и т.д.)
     * @param tiles Тайлы карты
     * @return Pixmap для сохранения в файл
     * */
    public static Pixmap generatePreview(Tiles tiles){
        Pixmap pixmap = new Pixmap(tiles.width, tiles.height);
        for(int x = 0; x < pixmap.width; x++){
            for(int y = 0; y < pixmap.height; y++){
                Tile tile = tiles.getn(x, y);
                if(tile.build != null) {
                    if(tile.build.config() instanceof Item) {
                        Item item = (Item) tile.build.config();
                        pixmap.set(x, pixmap.height - 1 - y, item.color.rgba());
                        item = null;
                    } else {
                        pixmap.set(x, pixmap.height - 1 - y, colorFor(tile.block(), tile.floor(), tile.overlay(), tile.team()));
                    }
                } else {
                    pixmap.set(x, pixmap.height - 1 - y, colorFor(tile.block(), tile.floor(), tile.overlay(), tile.team()));
                }
            }
        }
        return pixmap;
    }
    public static Map getMap(Fi file) {
        try{
            return MapIO.createMap(file, true);
        } catch (Exception e) {
            errorLogger.logErr(e);
            return null;
        }
    }
    public static void saveJson(String name, JsonValue payl) {
        try {
            Fi file = new Fi("./data/"+name.replace("/", "")+".json");
            file.parent().mkdirs();
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setOutputType(JsonWriter.OutputType.json);
            jsonWriter.object();
            for (JsonValue child : payl) {
                jsonWriter.name(child.name).value(child.asString());
            }
            jsonWriter.pop();
            file.writeString(writer.toString(), false);
        } catch (Exception e) {
            errorLogger.logErr(e);
        }
    }
    public static JsonValue loadJson(String name) {
        try {
            Fi file = new Fi("./data/"+name.replace("/", "")+".json");
            if (!file.exists() || file.isDirectory()) {
                return null;
            }
            return new JsonReader().parse(file);
        } catch (SerializationException e) {
            errorLogger.logErr(e);
            return null;
        }
    }
    public static void loadSettings() {
        joinMessage=Core.settings.getString("joinMessage", "");
        handledMessages=Core.settings.getLong("handledMessages", 0);
        handledCommands=Core.settings.getLong("handledCommands", 0);
        if(Core.settings.getString("bannedInSug") != null) {
            String[] bannedSug = Core.settings.getString("bannedInSug", "").split(";");
            for (String a : bannedSug) {
                bannedInSug.add(Snowflake.of(a));
            }
        }
    }
    public static void saveSettings() {
        Core.settings.put("joinMessage", joinMessage);
        Core.settings.put("handledMessages", handledMessages);
        Core.settings.put("handledCommands", handledCommands);
        if(!bannedInSug.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Snowflake f : bannedInSug) {
                sb.append(f.asString() + ";");
            }
            Core.settings.put("bannedInSug", sb.toString());
            sb.setLength(0);
        }
    }
    public static void loadNet() {
        Vars.net.handleClient(Connect.class, packet -> {
            Log.info("Generated packet for: @", packet.addressTCP);
            var c = new Packets.ConnectPacket();

            String uuid = randomString();
            String usid = randomString();
            int color = new Random().nextInt(999999);;
            String name = "EscoBot "+randomString(6);

            c.name = name;
            c.locale = Locale.getDefault().toString();
            c.mods = new Seq<>();
            c.mobile = Math.random() < 0.5;
            c.versionType = "official";
            c.color = color;
            c.usid = usid;
            c.uuid = uuid;
            Vars.net.send(c, true);

            Player zz = Player.create();
            zz.name = name;
            zz.locale = Locale.getDefault().toString();
            zz.color.set(color);
            Vars.player = zz;
        });
        Vars.net.handleClient(Disconnect.class, packet -> {
            if(packet.reason != null){
                switch(packet.reason) {
                    case "closed" -> Log.warn("disconnect.closed");
                    case "timeout" -> Log.warn("disconnect.timeout");
                    default -> Log.warn("disconnect.error");
                }
            }
            Vars.net.setClientLoaded(false);
            Vars.net.disconnect();
            Vars.player=null;
            Groups.clear();
        });
        Vars.net.handleClient(WorldStream.class, data -> {
            Log.info("Received world data: @ bytes.", data.stream.available());
            NetworkIO.loadWorld(new InflaterInputStream(data.stream));

            finishConnecting();
        });
        Vars.net.handleClient(SendMessageCallPacket.class, data -> {
            Log.info("Message packet!");
            Log.info(data.message);
        });
        Vars.net.handleClient(SendChatMessageCallPacket.class, data -> {
            Log.info("Chat packet!");
        });
        Vars.net.handleClient(SendMessageCallPacket2.class, data -> {
            Log.info("Message packet2!");
            if(data.playersender != null) {
                Log.info(data.playersender.name + ": " + data.message + "(" + data.unformatted + ")");
            } else {
                Log.info(data.message + "(" + data.unformatted + ")");
            }
        });
    }
    public static String randomString() {
        byte[] bytes = new byte[8];
        new Rand().nextBytes(bytes);
        return new String(Base64Coder.encode(bytes));
    }

    public static String randomString(int b) {
        byte[] bytes = new byte[b];
        new Rand().nextBytes(bytes);
        return new String(Base64Coder.encode(bytes));
    }

    public static void connectConfirmm() {
        /*
         * Call.connectConfirm()
         * */
        ConnectConfirmCallPacket packet = new ConnectConfirmCallPacket();
        Vars.net.send(packet, true);
    }

    public static void finishConnecting(){
        connectConfirmm();
        Vars.net.setClientLoaded(true);
    }
    public static void getAttach(Message message) {
        Attachment attachment = message.getAttachments().get(0);
        String urlStr = attachment.getUrl();
        String fileName = attachment.getFilename();
        Path savePath = Paths.get("data/atch", fileName);

        try {
            Files.createDirectories(savePath.getParent());
            try (InputStream in = new URL(urlStr).openStream()) {
                Files.copy(in, savePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            errorLogger.logErr(exception);
        }
    }
    public static void initKotlinScripting() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ktsEngine = manager.getEngineByExtension("kts");

            if (ktsEngine != null) {
                Log.info("Kotlin ScriptEngine initialized successfully.");
            } else {
                Log.warn("Failed to initialize Kotlin ScriptEngine.");
            }
        } catch (Throwable e) {
            Log.err(e);
        }
    }
    @Getter
    @Setter
    public static class emojiToName {
        String emoji;
        Object content;
        public emojiToName(String emoji, Object content) {
            if(content instanceof Item || content instanceof Block || content instanceof UnitType) {
                this.emoji = emoji;
                this.content = content;
            }
        }
    }
}
