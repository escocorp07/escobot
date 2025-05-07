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
    private BufferedImage renderTable(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        List<String[]> rows = new ArrayList<>();

        // Собираем заголовки и данные
        String[] headers = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            headers[i] = meta.getColumnLabel(i + 1);
        }
        rows.add(headers);

        while (rs.next()) {
            String[] row = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                row[i] = rs.getString(i + 1);
            }
            rows.add(row);
        }

        // Если строк больше, чем столбцов, меняем их местами (транспонируем)
        if (rows.size() - 1 > colCount) {
            rows = transpose(rows);
            colCount = rows.size() - 1;
        }

        // Настройки отступов и шрифта
        int cellPadding = 8;
        int rowHeight = 24;
        Font font = new Font("Monospaced", Font.PLAIN, 14);
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D gTmp = tmp.createGraphics();
        gTmp.setFont(font);
        FontMetrics fm = gTmp.getFontMetrics();

        // Вычисляем ширину столбцов
        int[] colWidths = new int[colCount];
        for (String[] row : rows) {
            for (int i = 0; i < colCount; i++) {
                String text = row[i] == null ? "null" : row[i];
                int width = fm.stringWidth(text) + 2 * cellPadding;
                colWidths[i] = Math.max(colWidths[i], width);
            }
        }

        // Максимальная ширина изображения (например, 1200px)
        int maxWidth = 1200;
        int width = Arrays.stream(colWidths).sum();

        // Если ширина таблицы больше максимальной, масштабируем её
        if (width > maxWidth) {
            double scale = (double) maxWidth / width;
            for (int i = 0; i < colCount; i++) {
                colWidths[i] = (int) (colWidths[i] * scale);
            }
            width = maxWidth;
        }

        // Вычисляем высоту изображения
        int height = rows.size() * rowHeight;

        // Создаем изображение
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height); // Фон
        g.setColor(Color.BLACK);

        // Рисуем строки таблицы
        for (int row = 0; row < rows.size(); row++) {
            int y = (row + 1) * rowHeight - 6;
            int x = 0;
            for (int col = 0; col < colCount; col++) {
                String cell = rows.get(row)[col];
                g.drawString(cell == null ? "null" : cell, x + cellPadding, y);
                x += colWidths[col];
            }
        }

        g.dispose();
        return img;
    }

    private List<String[]> transpose(List<String[]> original) {
        int rows = original.size();
        int cols = original.get(0).length;
        List<String[]> transposed = new ArrayList<>(cols);

        for (int i = 0; i < cols; i++) {
            String[] newRow = new String[rows];
            for (int j = 0; j < rows; j++) {
                newRow[j] = original.get(j)[i];
            }
            transposed.add(newRow);
        }
        return transposed;
    }
    public static boolean isValidUUID(String str) {
        return str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    }
}
