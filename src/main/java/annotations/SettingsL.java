package main.java.annotations;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SettingsL {
    String key() default ""; // если нужно переопределить название в сеттинге.
}