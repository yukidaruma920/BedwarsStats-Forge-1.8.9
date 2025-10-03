package com.yuki920.bedwarsstats;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class HypixelApiHandler {
    private static final Gson GSON = new Gson();
    private static final String HYPIXEL_API_URL = "https://api.hypixel.net/player?uuid=";
    private static final String KEY_CHECK_URL = "https://api.hypixel.net/key?key=";

    public static void checkApiKeyValidity() {
        if (ConfigHandler.apiKey == null || ConfigHandler.apiKey.isEmpty()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(KEY_CHECK_URL + ConfigHandler.apiKey);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() != 200) {
                     sendInvalidApiKeyMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void processPlayer(String username) {
        processPlayer(username, ConfigHandler.displayMode);
    }

    public static void processPlayer(String username, String modeStr) {
        CompletableFuture.runAsync(() -> {
            try {
                // Handle self-nick
                if (!ConfigHandler.nick.isEmpty() && username.equalsIgnoreCase(ConfigHandler.nick)) {
                    username = Minecraft.getMinecraft().thePlayer.getName();
                }

                String mojangUrl = "https://api.mojang.com/users/profiles/minecraft/" + username;
                String mojangResponse = sendHttpRequest(mojangUrl, null);
                if (mojangResponse == null) { return; }
                JsonObject mojangJson = GSON.fromJson(mojangResponse, JsonObject.class);
                if (mojangJson == null || !mojangJson.has("id")) { return; }
                String uuid = mojangJson.get("id").getAsString();

                if (ConfigHandler.apiKey.isEmpty()) {
                    sendMessageToPlayer("§cHypixel API Key not set! Use /bwm settings apikey <key> or the mod options menu.");
                    return;
                }
                String hypixelUrl = HYPIXEL_API_URL + uuid;
                String hypixelResponse = sendHttpRequest(hypixelUrl, ConfigHandler.apiKey);
                if (hypixelResponse == null) return;
                JsonObject hypixelJson = GSON.fromJson(hypixelResponse, JsonObject.class);

                if (!hypixelJson.get("success").getAsBoolean()) {
                    if (hypixelJson.has("cause") && hypixelJson.get("cause").getAsString().equals("Invalid API key")) {
                        sendInvalidApiKeyMessage();
                    }
                    return;
                }

                if (hypixelJson.get("player").isJsonNull()) { return; }
                JsonObject player = hypixelJson.getAsJsonObject("player");

                String formattedMessage = formatStats(player, modeStr);
                if (formattedMessage != null) {
                    sendMessageToPlayer(formattedMessage);
                }
            } catch (Exception e) {
                 e.printStackTrace();
            }
        });
    }

    private static void sendInvalidApiKeyMessage() {
        sendMessageToPlayer(EnumChatFormatting.RED + "Your Hypixel API key is invalid!");
        sendMessageToPlayer(EnumChatFormatting.YELLOW + "Please set a new one with /bwm setapikey <key> or in the mod options.");
    }

    private static String formatNumber(int number) {
        return String.format("%,d", number);
    }

    private static String getFkdrColor(double fkdr) {
        if (fkdr >= 20) return "§5"; if (fkdr >= 15) return "§d"; if (fkdr >= 10) return "§4";
        if (fkdr >= 8)  return "§c"; if (fkdr >= 6)  return "§6"; if (fkdr >= 4)  return "§e";
        if (fkdr >= 2)  return "§2"; if (fkdr >= 1)  return "§a"; if (fkdr >= 0.5) return "§f";
        return "§7";
    }

    private static String getWlrColor(double wlr) {
        if (wlr >= 10) return "§5"; if (wlr >= 8)  return "§d"; if (wlr >= 6)  return "§4";
        if (wlr >= 5)  return "§c"; if (wlr >= 4)  return "§6"; if (wlr >= 3)  return "§e";
        if (wlr >= 2)  return "§2"; if (wlr >= 1)  return "§a"; if (wlr >= 0.5) return "§f";
        return "§7";
    }

    private static String getWinsColor(int wins) {
        if (wins >= 50000) return "§5"; if (wins >= 25000) return "§d"; if (wins >= 10000) return "§4";
        if (wins >= 5000) return "§c"; if (wins >= 2500) return "§6"; if (wins >= 1000) return "§e";
        if (wins >= 500) return "§2";  if (wins >= 250) return "§a";  if (wins >= 50) return "§f";
        return "§7";
    }

    private static String getFinalsColor(int finals) {
        if (finals >= 100000) return "§5"; if (finals >= 50000) return "§d"; if (finals >= 25000) return "§4";
        if (finals >= 10000) return "§c"; if (finals >= 5000) return "§6"; if (finals >= 2500) return "§e";
        if (finals >= 1000) return "§2";  if (finals >= 500) return "§a";  if (finals >= 100) return "§f";
        return "§7";
    }

    private static String getModePrefix(String modeStr) {
        DisplayMode mode = DisplayMode.valueOf(modeStr.toUpperCase());
        switch (mode) {
            case SOLO: return "eight_one_";
            case DOUBLES: return "eight_two_";
            case THREES: return "four_three_";
            case FOURS: return "four_four_";
            default: return ""; // OVERALL
        }
    }

    private static int getStat(JsonObject bedwars, String key) {
        return bedwars.has(key) ? bedwars.get(key).getAsInt() : 0;
    }

    private static String formatStats(JsonObject player, String modeStr) {
        String username = player.get("displayname").getAsString();
        String rankPrefix = getRankPrefix(player);

        if (!player.has("stats") || player.get("stats").isJsonNull() || !player.getAsJsonObject("stats").has("Bedwars")) {
             return rankPrefix + username + "§7: No Bedwars stats found.";
        }

        JsonObject bedwars = player.getAsJsonObject("stats").getAsJsonObject("Bedwars");
        String modePrefix = getModePrefix(modeStr);

        int stars = (player.has("achievements") && player.getAsJsonObject("achievements").has("bedwars_level"))
                ? player.getAsJsonObject("achievements").get("bedwars_level").getAsInt() : 0;
        int wins = getStat(bedwars, modePrefix + "wins_bedwars");
        int losses = getStat(bedwars, modePrefix + "losses_bedwars");
        int finalKills = getStat(bedwars, modePrefix + "final_kills_bedwars");
        int finalDeaths = getStat(bedwars, modePrefix + "final_deaths_bedwars");

        double wlr = (losses == 0) ? wins : (double) wins / losses;
        double fkdr = (finalDeaths == 0) ? finalKills : (double) finalKills / finalDeaths;

        String prestige = PrestigeFormatter.formatPrestige(stars);
        String winsColor = getWinsColor(wins);
        String wlrColor = getWlrColor(wlr);
        String finalsColor = getFinalsColor(finalKills);
        String fkdrColor = getFkdrColor(fkdr);

        return String.format("%s %s%s§r: Wins %s%s§r | WLR %s%.2f§r | Finals %s%s§r | FKDR %s%.2f",
                prestige,
                rankPrefix,
                username,
                winsColor, formatNumber(wins),
                wlrColor, wlr,
                finalsColor, formatNumber(finalKills),
                fkdrColor, fkdr);
    }

    private static String getRankPrefix(JsonObject player) {
        String rank = player.has("rank") && !player.get("rank").getAsString().equals("NORMAL") ? player.get("rank").getAsString() : null;
        String monthlyPackageRank = player.has("monthlyPackageRank") && !player.get("monthlyPackageRank").getAsString().equals("NONE") ? player.get("monthlyPackageRank").getAsString() : null;
        String newPackageRank = player.has("newPackageRank") && !player.get("newPackageRank").getAsString().equals("NONE") ? player.get("newPackageRank").getAsString() : null;
        String rankPlusColorStr = player.has("rankPlusColor") ? player.get("rankPlusColor").getAsString() : null;

        if (rank != null) {
            switch (rank) {
                case "YOUTUBER": return "§c[§fYOUTUBE§c] ";
                case "STAFF": return "§c[§6STAFF§c] ";
            }
        }

        String displayRank = monthlyPackageRank != null ? monthlyPackageRank : newPackageRank;
        if (displayRank == null) { return "§7"; }

        String plusColor = "§c";
        if (rankPlusColorStr != null) {
            EnumChatFormatting color = EnumChatFormatting.getValueByName(rankPlusColorStr);
            if (color != null) plusColor = color.toString();
        }

        switch (displayRank) {
            case "VIP": return "§a[VIP] ";
            case "VIP_PLUS": return "§a[VIP§6+§a] ";
            case "MVP": return "§b[MVP] ";
            case "MVP_PLUS": return "§b[MVP" + plusColor + "+§b] ";
            case "SUPERSTAR": return "§6[MVP" + plusColor + "++§6] ";
            default: return "§7";
        }
    }

    private static String sendHttpRequest(String urlString, String apiKey) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        if (apiKey != null) {
            connection.setRequestProperty("API-Key", apiKey);
        }
        if (connection.getResponseCode() == 200) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                return content.toString();
            }
        }
        return null;
    }

    private static void sendMessageToPlayer(String message) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
            }
        });
    }
}