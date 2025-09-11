package com.yuki920.bedwarsstats;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class ConfigHandler {
    private static Configuration config;
    public static String apiKey;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        syncConfig();
    }

    public static void syncConfig() {
        config.load();
        apiKey = config.getString("apiKey", "General", "", "Your Hypixel API Key. Get it with /api new.");
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void setApiKey(String key) {
        apiKey = key;
        config.get("General", "apiKey", "").set(key);
        config.save();
    }
}
