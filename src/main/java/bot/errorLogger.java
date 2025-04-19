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

/**Логер ошибок и не только.*/
public class errorLogger {
    public static void logErr(Throwable error) {
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Path logPath = Paths.get("logs/errors/log-" + date + ".txt");
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

    public static void debug(Object o) {
        if(debug)
            Log.info("D", o);
    }
}
