package main.java.site;

import arc.struct.Seq;
import io.javalin.http.Context;
import main.java.Main;

import java.io.*;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static main.java.BVars.*;

public class Routes {
    public static Seq<String> sitemapRoutes = new Seq<>();
    public static void loadRoutes() {
        site.before(h->{
            incrementReqHandled();
        });

        get("/", ctx->{
            try {
                ctx.html(readFileFromJar("public/index/index.html"));
            } catch (IOException e) {
                ctx.status(521).result("");
            }
        });

        site.get("/favicon.ico", ctx->{
            File localFile = new File("cdn/favicon.gif");
            ctx.contentType("image/gif");
            // code stolen from GitHub issue
            InputStream is = new BufferedInputStream(new FileInputStream(localFile));
            ctx.header("Content-Disposition", "attachment; filename=\""+localFile.getName()+"\"");
            ctx.header("Content-Length", String.valueOf(localFile.length()));
            ctx.status(200).result(is);
        });

        site.get("/sitemap.xml", ctx->{
            ctx.contentType("application/xml");
            StringBuilder sitemap = new StringBuilder();
            sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

            for (String route : sitemapRoutes) {
                sitemap.append("  <url>\n");
                sitemap.append("    <loc>https://grely.icu" + route + "</loc>\n");
                sitemap.append("    <lastmod>2025-05-02</lastmod>\n");
                sitemap.append("    <changefreq>weekly</changefreq>\n");
                sitemap.append("    <priority>0.8</priority>\n");
                sitemap.append("  </url>\n");
            }

            sitemap.append("</urlset>");

            ctx.result(sitemap.toString());
            sitemap.setLength(0);
        });
        site.error(404, ctx->{
            ctx.status(404).result("Not found.");
        });
    }
    public static void get(String path, Consumer<Context> code) {
        sitemapRoutes.add(path);
        site.get(path, code::accept);
    }
    public static void sendFile(Context ctx, String filename) {
        try {
            File localFile = new File(filename);
            ctx.contentType("image/gif");
            // code stolen from GitHub issue
            InputStream is = new BufferedInputStream(new FileInputStream(localFile));
            ctx.header("Content-Disposition", "attachment; filename=\"" + localFile.getName() + "\"");
            ctx.header("Content-Length", String.valueOf(localFile.length()));
            ctx.status(200).result(is);
        } catch (Exception e) {
            ctx.status(404).result("File not found.");
        }
    }
    public static String read(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            return null;
        }
    }
    public static void sendFileFromJar(Context ctx, String filename) {
        try {
            InputStream is = Main.class.getResourceAsStream("/" + filename);
            if (is == null) {
                ctx.status(404).result("File not found in JAR.");
                return;
            }
            String contentType = URLConnection.guessContentTypeFromName(filename);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            ctx.contentType(contentType);

            ctx.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            ctx.status(200).result(is);
        } catch (Exception e) {
            ctx.status(404).result("Not found.");
        }
    }
    public static String readFileFromJar(String fileName) throws IOException {
        InputStream inputStream = Main.class.getResourceAsStream("/" + fileName);

        if (inputStream == null) {
            throw new IOException("Файл \"" + fileName + "\" не найден в JAR.");
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
        }

        return content.toString();
    }
}
