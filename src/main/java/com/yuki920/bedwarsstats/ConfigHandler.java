package com.yuki920.bedwarsstats;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class ConfigHandler {

    public static Configuration config;

    // Categories
    public static final String CATEGORY_GENERAL = "general";

    // Options
    public static String apiKey = "";
    public static String displayMode = "OVERALL";
    public static String nick = "";

    public static void init(File configFile) {
        config = new Configuration(configFile);
        syncConfig();
    }

    public static void syncConfig() {
        config.load();
        apiKey = config.getString("apiKey", CATEGORY_GENERAL, "", "Your Hypixel API Key. Get it with /api new.");
        displayMode = config.getString("displayMode", CATEGORY_GENERAL, "OVERALL", "The default display mode for Bedwars stats. [OVERALL, SOLO, DOUBLES, THREES, FOURS]");
        nick = config.getString("nick", CATEGORY_GENERAL, "", "Your registered nickname on Hypixel.");
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void setApiKey(String key) {
        apiKey = key;
        if (config != null) {
            config.get(CATEGORY_GENERAL, "apiKey", "").set(key);
            config.save();
        }
    }

    public static void setDisplayMode(String mode) {
        displayMode = mode.toUpperCase();
        if (config != null) {
            config.get(CATEGORY_GENERAL, "displayMode", "OVERALL").set(mode.toUpperCase());
            config.save();
        }
    }

    public static void setNick(String nickname) {
        nick = nickname;
        if (config != null) {
            config.get(CATEGORY_GENERAL, "nick", "").set(nickname);
            config.save();
        }
    }
}