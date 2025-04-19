package main.java;

import reactor.util.Logger;
import java.util.function.Function;

public class LoggerProvider implements Function<String, Logger>{
    @Override
    public Logger apply(String string) {
        return new CustomLogger();
    }
}
