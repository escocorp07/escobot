package main.java;

import arc.files.Fi;
import arc.util.Log;
import arc.util.serialization.JsonReader;
import discord4j.common.util.Snowflake;

public class ConfigLoader {
    public static void loadcfg() {
        try {
             Fi file = new Fi("./config.json");
            if(!file.exists() || file.isDirectory()){
                Log.err("No config file found!");
                return;
            }
            JsonReader JSON = new JsonReader();
            var value = JSON.parse(file);
            BVars.btoken = value.getString("token");
            BVars.prefix = value.getString("prefix");
            BVars.guild = Snowflake.of(value.getLong("guild"));
            BVars.ownerid = value.getLong("owner_role");
            if(!BVars.prefix.endsWith(".")) BVars.prefix += ".";
            Log.info("Config loaded!");
        } catch (Exception e) {
            Log.err("Error while trying to load config!", e);
        }
    }

}
