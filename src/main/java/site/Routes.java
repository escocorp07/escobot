package main.java.site;

import static main.java.BVars.*;

public class Routes {
    public static void loadRoutes() {
        site.before(h->{
            incrementReqHandled();
        });
        site.get("/", ctx->{
            ctx.status(200).result("It works!");
        });
        site.error(404, ctx->{
            ctx.status(404).result("Not found.");
        });
    }
}
