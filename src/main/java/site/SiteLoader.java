package main.java.site;

import arc.util.Log;
import arc.util.Threads;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.http.staticfiles.Location;

import static main.java.BVars.port;
import static main.java.BVars.site;
import static main.java.site.Routes.loadRoutes;

/*
precompress
 * если браузер поддерживает сжатие и у тебя в cdn есть
 * файл.png и есть файл.gz
 * юзер сделал запрос на файл.png
 * а отправило сразу файл.gz,
 * мол ты сжал сам его и жавалину утруждаться не нужно
 * и сжимать уже нужно
 * */

public class SiteLoader {
    static String certPath = "/etc/letsencrypt/live/grely.icu/fullchain.pem"; // Путь к файлу сертификата
    static String keyPath = "/etc/letsencrypt/live/grely.icu/privkey.pem";
    public static void load() {
            site = Javalin.create(config -> {
                config.staticFiles.add(staticf->{
                    staticf.directory="public";
                    staticf.hostedPath="/public";
                    staticf.precompress=false;
                    staticf.location=Location.CLASSPATH;
                });
                config.staticFiles.add(staticf->{
                    staticf.directory="cdn";
                    staticf.hostedPath="/cdn";
                    staticf.precompress=false;
                    staticf.location=Location.EXTERNAL;
                });
                SslPlugin plugin = new SslPlugin(conf->{
                    conf.pemFromPath(certPath, keyPath);
                });
                config.registerPlugin(plugin);
            });
            loadRoutes();
            Threads.daemon("Site", ()->{
                Log.info("Starting site!");
                site.start(port);
            });
    }
}
