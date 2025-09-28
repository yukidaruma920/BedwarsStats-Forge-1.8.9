package com.yuki920.bedwarsstats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yuki920.bedwarsstats.libs.moulconfig.Config;
import com.yuki920.bedwarsstats.libs.moulconfig.MoulConfig;
import com.yuki920.bedwarsstats.libs.moulconfig.annotations.ConfigEditorType;
import com.yuki920.bedwarsstats.libs.moulconfig.annotations.ConfigOption;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigHandler extends Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File configFile;

    @ConfigOption(name = "API Key", desc = "Your Hypixel API Key. Get it with /api new.")
    @ConfigEditorType(value = "TEXT_FIELD")
    public String apiKey = "";

    @ConfigOption(name = "Display Mode", desc = "The default display mode for Bedwars stats.")
    @ConfigEditorType(value = "ENUM_DROPDOWN")
    public DisplayMode displayMode = DisplayMode.OVERALL;

    @ConfigOption(name = "Nickname", desc = "Your registered nickname on Hypixel.")
    @ConfigEditorType(value = "TEXT_FIELD")
    public String nick = "";

    public ConfigHandler(File configFile) {
        this.configFile = configFile;
        load();
    }

    public void load() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    this.apiKey = data.apiKey;
                    this.displayMode = data.displayMode;
                    this.nick = data.nick;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(new ConfigData(this), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTitle() {
        return "Bedwars Stats " + BedwarsStats.VERSION;
    }

    // A separate class to hold the data for serialization,
    // to avoid serializing the entire Config object.
    private static class ConfigData {
        public String apiKey;
        public DisplayMode displayMode;
        public String nick;

        public ConfigData(ConfigHandler handler) {
            this.apiKey = handler.apiKey;
            this.displayMode = handler.displayMode;
            this.nick = handler.nick;
        }
    }
}