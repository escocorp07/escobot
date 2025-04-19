package main.java;

import arc.util.Log;
import arc.util.Strings;
import main.java.bot.errorLogger;
import reactor.util.Logger;
public class CustomLogger implements Logger {
    @Override
    public String getName() {
        return "Main Loger";
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String s) {
    }

    @Override
    public void trace(String s, Object... objects) {
    }

    @Override
    public void trace(String s, Throwable throwable) {
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String s) {
        errorLogger.debug(s);
    }

    @Override
    public void debug(String s, Object... objects) {
        errorLogger.debug(s, objects);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        errorLogger.debug(s+"\n"+throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String s) {
        Log.info(s);
    }

    @Override
    public void info(String s, Object... objects) {
        Log.info(s, objects);
    }

    @Override
    public void info(String s, Throwable throwable) {
        Log.info(s, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String s) {
        Log.warn(s);
    }

    @Override
    public void warn(String s, Object... objects) {
        Log.warn(s, objects);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        Log.warn(s, throwable.toString());
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String s) {
        Log.err(s);
    }

    @Override
    public void error(String s, Object... objects) {
        Log.err(s, objects);
    }

    @Override
    public void error(String s, Throwable throwable) {
        Log.err(s, throwable);
    }
}