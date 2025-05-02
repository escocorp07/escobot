package main.java.site;

import static main.java.BVars.*;

public class Routes {
    public static void loadRoutes() {
        site.before(h->{
            handledRequests+=1;
        });
        site.get("/", ctx->{
            ctx.status(200).result("It works!");
        });
    }
}
