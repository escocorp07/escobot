package main.java;

import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.util.Log;
import arc.util.serialization.*;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import main.java.annotations.SettingsL;
import main.java.bot.errorLogger;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import arc.*;
import arc.math.Rand;
import arc.struct.Seq;
import arc.util.serialization.Base64Coder;

import java.awt.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.StringWriter;
import java.nio.file.Files;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static main.java.BVars.maxHeight;
import static main.java.BVars.maxWidth;
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
    /*public static void loadSettings() {
        joinMessage=Core.settings.getString("joinMessage", "Думай.");
        presence=Core.settings.getString("presence", "");
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
        Core.settings.put("presence", presence);
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
    }*/
    public static void loadSettings() {
        for (Field field : BVars.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(SettingsL.class)) {
                field.setAccessible(true);
                SettingsL annotation = field.getAnnotation(SettingsL.class);
                String key = annotation.key().isEmpty() ? field.getName() : annotation.key();

                try {
                    Class<?> type = field.getType();
                    if (type == String.class) {
                        field.set(null, Core.settings.getString(key, (String) field.get(null)));
                    } else if (type == long.class || type == Long.class) {
                        field.set(null, Core.settings.getLong(key, field.getLong(null)));
                    } else if (type == int.class || type == Integer.class) {
                        field.set(null, Core.settings.getInt(key, field.getInt(null)));
                    } else if (Seq.class.isAssignableFrom(type)) {
                        Type genericType = field.getGenericType();
                        if (genericType instanceof ParameterizedType parameterizedType) {
                            Type arg = parameterizedType.getActualTypeArguments()[0];
                            String raw = Core.settings.getString(key, "");
                            Seq<Object> list = new Seq<>();
                            if (!raw.isEmpty()) {
                                for (String entry : raw.split(";")) {
                                    if (arg == Snowflake.class) {
                                        list.add(Snowflake.of(entry));
                                    } else if (arg == String.class) {
                                        list.add(Base64Coder.decodeString(entry));
                                    } else if (arg == Long.class || arg == long.class) {
                                        list.add(Long.parseLong(entry));
                                    } else if (arg == Integer.class || arg == int.class) {
                                        list.add(Integer.parseInt(entry));
                                    } // можно добавить другие типы при необходимости, но я не думаю, что она будет.
                                }
                            }
                            field.set(null, list);
                        }
                    }
                } catch (Exception e) {
                    Log.err(e);
                }
            }
        }
    }
    public static void saveSettings() {
        for (Field field : BVars.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(SettingsL.class)) {
                field.setAccessible(true);
                SettingsL annotation = field.getAnnotation(SettingsL.class);
                String key = annotation.key().isEmpty() ? field.getName() : annotation.key();

                try {
                    Object value = field.get(null);
                    if (value instanceof String str) {
                        Core.settings.put(key, str);
                    } else if (value instanceof Long l) {
                        Core.settings.put(key, l);
                    } else if (value instanceof Integer i) {
                        Core.settings.put(key, i);
                    } else if (value instanceof Seq<?> seq) {
                        if (!seq.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (Object obj : seq) {
                                if (obj instanceof Snowflake s) {
                                    sb.append(s.asString());
                                } else {
                                    sb.append(Base64Coder.encodeString(obj.toString()));
                                }
                                sb.append(';');
                            }
                            Core.settings.put(key, sb.toString());
                        }
                    } else if(value instanceof Boolean) {
                        boolean b = (Boolean) value;
                        Core.settings.put(key, b);
                    }
                } catch (Exception e) {
                    Log.err(e);
                }
            }
        }
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
    public static void getAttach(Attachment attachment) {
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
    public static BufferedImage renderTable(ResultSet rs) throws SQLException {
        int width = maxWidth;
        int height = maxHeight;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));

        int x = 20;
        int y = 20;
        int rowHeight = 20;
        int columnWidth = 150;

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            g.drawString(columnName, x + (i - 1) * columnWidth, y);
        }

        y += rowHeight;

        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = rs.getString(i);
                g.drawString(columnValue, x + (i - 1) * columnWidth, y);
            }
            y += rowHeight;
        }

        g.dispose();

        return image;
    }


    public static boolean isValidUUID(String str) {
        return str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    }
}
