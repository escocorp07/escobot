package main.java.site;

import arc.struct.Seq;
import io.javalin.http.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
            ctx.status(200).result("It works!");
        });

        site.get("/favicon.ico", ctx->{
            File localFile = new File("cdn/favicon.gif");
            //ctx.contentType("image/gif");
            // code stolen from github issue
            InputStream is = new BufferedInputStream(new FileInputStream(localFile));
            ctx.header("Content-Dispotion", "attachment; filename=\""+localFile.getName()+"\"");
            ctx.header("Content-Length", String.valueOf(localFile.length()));
            ctx.result(is);
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
}
