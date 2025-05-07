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

import javax.swing.*;
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
import java.util.Collections;
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
        // Создаем список для хранения данных таблицы
        List<String[]> data = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        // Извлекаем метаданные (столбцы)
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Сохраняем названия столбцов
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }

        // Считываем строки из ResultSet
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getString(i);
            }
            data.add(row);
        }

        // Если количество столбцов меньше количества строк, меняем местами
        if (columnCount < data.size()) {
            // Транспонируем данные
            List<String[]> transposed = new ArrayList<>();
            int maxLength = data.stream().mapToInt(row -> row.length).max().orElse(0);

            for (int i = 0; i < maxLength; i++) {
                String[] transposedRow = new String[data.size()];
                for (int j = 0; j < data.size(); j++) {
                    transposedRow[j] = data.get(j)[i];
                }
                transposed.add(transposedRow);
            }
            data = transposed;

            // Меняем местами названия столбцов
            Collections.swap(columns, 0, 1);
        }

        // Создаем HTML таблицу
        StringBuilder html = new StringBuilder();
        html.append("<html><body><table border='1' cellspacing='0' cellpadding='5'>");

        // Добавляем заголовки столбцов
        html.append("<tr>");
        for (String column : columns) {
            html.append("<th>").append(column).append("</th>");
        }
        html.append("</tr>");

        // Добавляем строки данных
        for (String[] row : data) {
            html.append("<tr>");
            for (String cell : row) {
                html.append("<td>").append(cell != null ? cell : "").append("</td>");
            }
            html.append("</tr>");
        }

        html.append("</table></body></html>");

        // Рендерим HTML в BufferedImage
        JEditorPane editorPane = new JEditorPane("text/html", html.toString());
        editorPane.setSize(maxWeight, maxHeight);  // Устанавливаем размер, который ограничивает
        editorPane.setPreferredSize(new Dimension(maxWeight, maxHeight));

        // Вычисляем требуемый размер для изображения с учетом контента
        int imageWidth = Math.min(maxWeight, editorPane.getPreferredSize().width);
        int imageHeight = Math.min(maxHeight, editorPane.getPreferredSize().height);

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        editorPane.print(graphics);  // Рендерим в графику

        graphics.dispose();
        return image;
    }
    public static boolean isValidUUID(String str) {
        return str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    }
}
