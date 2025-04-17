package main.java.bot;

import arc.util.Log;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class errorLogger {
    public static void logErr(Throwable error) {
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Path logPath = Paths.get("logs/errors/log-" + date + ".txt");
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
}
