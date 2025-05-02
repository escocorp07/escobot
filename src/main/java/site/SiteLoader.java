package main.java.site;

import arc.util.Log;
import arc.util.Threads;
import io.javalin.Javalin;

import static main.java.BVars.*;
import static main.java.site.Routes.*;

public class SiteLoader {
    public static void load() {
            site = Javalin.create(config -> {
                config.staticFiles.add(staticf->{
                    staticf.directory = "cdn";
                    staticf.hostedPath = "cdn";
                    /*
                    * если браузер поддерживает сжатие и у тебя в cdn есть
                    * файл.png и есть файл.gz
                    * юзер сделал запрос на файл.png
                    * а отправило сразу файл.gz,
                    * мол ты сжал сам его и жавалину утруждаться не нужно
                    * и сжимать уже нужно
                    * */
                    staticf.precompress=true;
                });
            });
            loadRoutes();
            Threads.daemon(()->{
                Log.info("Starting site!");
                site.start(port);
            });
    }
}
