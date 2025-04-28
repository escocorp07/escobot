package main.java.bot;

import arc.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import static main.java.BVars.*;
import static main.java.bot.utils.randomString;

/**Логер ошибок и не только.*/
public class errorLogger {
    /**Записать ошибку в файл.*/
    public static void logErr(Throwable error) {
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Path logPath = Paths.get("logs/errors/log-"+date.replace(" ", "-")+randomString(16)+".txt");
            Log.warn("New error, path: logs/errors/log-@.txt", date);
            Files.createDirectories(logPath.getParent());
            if (!Files.exists(logPath)) {
                Files.createFile(logPath);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(logPath, StandardOpenOption.APPEND)) {
                writer.write("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] ");
                writer.write(error.toString());
                writer.newLine();
                for (StackTraceElement element : error.getStackTrace()) {
                    writer.write("    at " + element.toString());
                    writer.newLine();
                }
                writer.newLine();
            }
        } catch (IOException ioException) {
            Log.err(ioException);
        }
    }
    /**Дебаг сообщение видное только при включенном дебаге.*/
    public static void debug(Object o) {
        if(debug)
            System.out.println("\033[32m[D]\033[0m " + o);
    }
    /**Дебаг сообщение видное только при включенном дебаге.*/
    public static void debug(String s, Object... o) {
        if(debug)
            System.out.println("\033[32m[D]\033[0m " + s +" "+o);
    }
    /**Дебаг сообщение видное только при включенном дебаге.*/
    public static void debug(Object... o) {
        if(debug)
            System.out.println("\033[32m[D]\033[0m " + o);
    }
}
