package main.java.site;

import arc.struct.Seq;
import arc.util.Log;
import discord4j.core.spec.MessageCreateSpec;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import main.java.Main;
import main.java.bot.errorLogger;
import org.apache.commons.net.util.SubnetUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static main.java.BVars.*;
import static main.java.bot.botUtils.sendMessage;
import static main.java.bot.utils.randomString;
import static main.java.Database.DatabseConnector.*;

public class Routes {
    public static Seq<String> sitemapRoutes = new Seq<>();
    static Seq<String> cloudflareCIDRs = Seq.with(
            "173.245.48.0/20",
            "103.21.244.0/22",
            "103.22.200.0/22",
            "103.31.4.0/22",
            "141.101.64.0/18",
            "108.162.192.0/18",
            "190.93.240.0/20",
            "188.114.96.0/20",
            "197.234.240.0/22",
            "198.41.128.0/17",
            "162.158.0.0/15",
            "104.16.0.0/13",
            "104.24.0.0/14",
            "172.64.0.0/13",
            "131.0.72.0/22"
    );
    public static void loadRoutes() {
        site.before(ctx->{
            try {
                String ip = ctx.ip();
                /*if (ip == null) {
                    ctx.status(403).result("Forbidden.");
                    ctx.skipRemainingHandlers();
                    return;
                }*/
                if(!isCloudflareIP(ip)) {
                    ctx.status(403).result("Forbidden.");
                    ctx.skipRemainingHandlers();
                    return;
                }
                incrementReqHandled();
                String headers = ctx.headerMap().entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining("\n"));
                String body = "";
                StringBuilder sb = new StringBuilder();
                sb.append("```");
                sb.append("URL: "+ctx.fullUrl());
                sb.append("\nMethod: " + ctx.method())
                                .append("\nHeaders: \n"+headers.replace("`", ""))
                                        .append("\nBody: \n"+body.replace("`", ""));
                sb.setLength(1997);
                if(!sb.toString().endsWith("```"))
                    sb.append("```");
                sendMessage("1330050716928049262", sb.toString());
            } catch (Exception ex) {
                errorLogger.logErr(ex);
            }
        });

        get("/", ctx->{
            try {
                ctx.html(readFileFromJar("public/index/index.html"));
            } catch (IOException e) {
                ctx.status(500).result("");
            }
        });
        get("/mindustry_guide", ctx->{
            try {
                ctx.html(readFileFromJar("public/mindustry_guide/index.html"));
            } catch (IOException e) {
                ctx.status(500).result("");
            }
        });
        get("/appeal", ctx->{
            try {
                ctx.html(readFileFromJar("public/appeals/index.html"));
            } catch (IOException e) {
                ctx.status(500).result("");
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
        // backend
        site.post("/submit-appeal", ctx->{
            StringBuilder sb = new StringBuilder();
            String banId = ctx.formParam("ban_id");
            String proof = ctx.formParam("proof");
            if (banId == null || banId.isEmpty()) {
                ctx.status(400).result("Missing ban_id");
                return;
            }
            if (proof == null || proof.isEmpty()) {
                ctx.status(400).result("Missing proof");
                return;
            }
            try {
                Integer.parseInt(banId);
            } catch (NumberFormatException ex) {
                ctx.status(400).result("Ban_ID not integer!");
                return;
            }
            String ip;
            ip=ctx.header("CF-Connecting-IP");
            if(ip==null)
                ip=ctx.header("X-Forwarded-For");
            if(ip==null)
                ip=ctx.ip();
            sb.append("```\n");
            sb.append("New appeal\nIP:"+ip+"\n");
            sb.append("Ban_ID: "+banId.replace("`", ""))
                            .append("\nproof: "+proof.replace("`", ""));
            sb.setLength(1997);
            sb.append("```");
            final int appeal_id = createAppeal(ip, proof, Integer.parseInt(banId)).orElse(0);
            if(appeal_id != 0) {
                var var = new Object() {
                    public int appealID = appeal_id;
                };
                ctx.status(200).json(var);
                sendMessage("1368010923503128637", sb.toString());
            } else
                ctx.status(500).result("Unable to create appeal.");
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
    public static boolean isCloudflareIP(String ip) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            for (String cidr : cloudflareCIDRs) {
                SubnetUtils subnetUtils = new SubnetUtils(cidr);
                subnetUtils.setInclusiveHostCount(true);
                if (subnetUtils.getInfo().isInRange(inetAddress.getHostAddress())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
