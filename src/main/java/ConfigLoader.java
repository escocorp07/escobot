package main.java;

import arc.files.Fi;
import arc.util.Log;
import arc.util.serialization.JsonReader;
import discord4j.common.util.Snowflake;

public class ConfigLoader {
    /**Подгрузить конфиг.*/
    public static void loadcfg() {
        try {
             Fi file = new Fi("./config.json");
            if(!file.exists() || file.isDirectory()){
                Log.err("No config file found!");
                System.exit(2);
                return;
            }
            JsonReader JSON = new JsonReader();
            var value = JSON.parse(file);
            BVars.btoken = value.getString("token");
            BVars.prefix = value.getString("prefix");
            BVars.guild = Snowflake.of(value.getLong("guild"));
            BVars.ownerid = value.getLong("owner_role");
            BVars.reactionMessage = value.getLong("reaction_message");
            BVars.newsid = value.getLong("news_role");
            BVars.grelyid = value.getLong("grelyid");
            BVars.forumBannedid = value.getLong("forum_banned_id");
            BVars.arrivalsid = value.getLong("arrivals_id");
            BVars.sugid = value.getLong("suggestions_id");
            BVars.debug = value.getBoolean("debug", false);
            BVars.sugping= value.getLong("suggestions_ping_id");
            BVars.sugpingrole= value.getLong("suggestions_role_id");
            BVars.DB_USER=value.getString("DB_USER");
            BVars.DB_PASSWORD=value.getString("DB_PASSWORD");
            if(!BVars.prefix.endsWith(".")) BVars.prefix += ".";
            Log.info("Config loaded!");
        } catch (Exception e) {
            Log.err("Error while trying to load config!", e);
        }
    }

}
