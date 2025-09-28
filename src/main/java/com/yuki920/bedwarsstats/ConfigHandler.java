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
        // Load the configuration file
        config.load();

        // Read values from the config
        apiKey = config.getString("apiKey", CATEGORY_GENERAL, "", "Your Hypixel API Key. Get it with /api new.");
        displayMode = config.getString("displayMode", CATEGORY_GENERAL, "OVERALL", "The default display mode for Bedwars stats. [OVERALL, SOLO, DOUBLES, THREES, FOURS]");
        nick = config.getString("nick", CATEGORY_GENERAL, "", "Your registered nickname on Hypixel.");

        // Save the configuration file if it has changed.
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void setApiKey(String key) {
        config.get(CATEGORY_GENERAL, "apiKey", "").set(key);
        config.save();
        apiKey = key;
    }

    public static void setDisplayMode(String mode) {
        config.get(CATEGORY_GENERAL, "displayMode", "OVERALL").set(mode.toUpperCase());
        config.save();
        displayMode = mode.toUpperCase();
    }

    public static void setNick(String nickname) {
        config.get(CATEGORY_GENERAL, "nick", "").set(nickname);
        config.save();
        nick = nickname;
    }
}