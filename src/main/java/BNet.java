package main.java;

import arc.util.Log;
import main.java.bot.errorLogger;
import mindustry.net.Net;

import static main.java.BVars.*;

public class BNet extends Net {
    public BNet(NetProvider provider) {
        super(provider);
    }
    @Override
    public void handleException(Throwable e) {
        showError(e);
    }
    @Override
    public void showError(Throwable e){
        if(e.getMessage() != null){
            String msg = e.getMessage().toLowerCase();
            if(bannedErrs.find(banned -> msg.contains(banned.toLowerCase())) == null){
                errorLogger.logErr(e);
            }
        }
    }
}