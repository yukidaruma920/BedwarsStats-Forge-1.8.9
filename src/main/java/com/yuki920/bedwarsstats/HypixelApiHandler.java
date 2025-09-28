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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HypixelApiHandler {
    private static final Gson GSON = new Gson();
    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String HYPIXEL_API_URL = "https://api.hypixel.net/player?uuid=";
    private static final String KEY_CHECK_URL = "https://api.hypixel.net/key?key=";

    // Overloaded method for commands that don't specify a mode
    public static void processPlayer(String username) {
        processPlayer(username, BedwarsStats.config.displayMode);
    }

    public static void processPlayer(String username, DisplayMode mode) {
        CompletableFuture.runAsync(() -> {
            try {
                // Handle self-nick
                if (username.equalsIgnoreCase(BedwarsStats.config.nick)) {
                    username = Minecraft.getMinecraft().thePlayer.getName();
                }

                String uuid = getUuid(username);
                if (uuid == null) {
                    sendMessageToPlayer(EnumChatFormatting.YELLOW + username + EnumChatFormatting.RESET + " is nicked, stats cannot be retrieved.");
                    return;
                }

                String apiKey = BedwarsStats.config.apiKey;
                if (apiKey.isEmpty()) {
                    sendMessageToPlayer(EnumChatFormatting.RED + "Hypixel API Key not set! Use /bwm to set it.");
                    return;
                }

                String hypixelUrl = HYPIXEL_API_URL + uuid;
                String hypixelResponse = sendHttpRequest(hypixelUrl, apiKey);
                if (hypixelResponse == null) return;

                JsonObject hypixelJson = GSON.fromJson(hypixelResponse, JsonObject.class);

                if (!hypixelJson.get("success").getAsBoolean()) {
                    if (hypixelJson.has("cause") && hypixelJson.get("cause").getAsString().equals("Invalid API key")) {
                        sendInvalidApiKeyMessage();
                    }
                    return;
                }

                if (hypixelJson.get("player").isJsonNull()) {
                    sendMessageToPlayer(EnumChatFormatting.YELLOW + username + EnumChatFormatting.RESET + " has never joined Hypixel.");
                    return;
                }

                JsonObject player = hypixelJson.getAsJsonObject("player");
                String formattedMessage = formatStats(player, mode);
                sendMessageToPlayer(formattedMessage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void checkApiKeyValidity() {
        CompletableFuture.runAsync(() -> {
            try {
                String apiKey = BedwarsStats.config.apiKey;
                if (apiKey == null || apiKey.isEmpty()) {
                    return; // Don't check if no key is set
                }
                String url = KEY_CHECK_URL + apiKey;
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() != 200) {
                     sendInvalidApiKeyMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private static String getUuid(String username) throws Exception {
        String mojangUrl = MOJANG_API_URL + username;
        String mojangResponse = sendHttpRequest(mojangUrl, null);
        if (mojangResponse == null) {
            return null; // Player likely doesn't exist or API is down
        }
        JsonObject mojangJson = GSON.fromJson(mojangResponse, JsonObject.class);
        return mojangJson.get("id").getAsString();
    }

    private static String formatStats(JsonObject player, DisplayMode mode) {
        String username = player.get("displayname").getAsString();
        String rankPrefix = getRankPrefix(player);

        if (!player.has("stats") || player.get("stats").isJsonNull() || !player.getAsJsonObject("stats").has("Bedwars")) {
            return rankPrefix + username + "§7: No Bedwars stats found.";
        }

        JsonObject bedwars = player.getAsJsonObject("stats").getAsJsonObject("Bedwars");
        String modePrefix = getModePrefix(mode);

        int stars = player.has("achievements") && player.getAsJsonObject("achievements").has("bedwars_level")
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
                prestige, rankPrefix, username,
                winsColor, String.format("%,d", wins),
                wlrColor, wlr,
                finalsColor, String.format("%,d", finalKills),
                fkdrColor, fkdr);
    }

    private static int getStat(JsonObject bedwars, String key) {
        return bedwars.has(key) ? bedwars.get(key).getAsInt() : 0;
    }

    private static String getModePrefix(DisplayMode mode) {
        switch (mode) {
            case SOLO: return "eight_one_";
            case DOUBLES: return "eight_two_";
            case THREES: return "four_three_";
            case FOURS: return "four_four_";
            default: return ""; // Overall
        }
    }

    // Color and Rank methods (getFkdrColor, getWlrColor, etc.) remain the same
    // They are omitted here for brevity but are assumed to be present.
        private static String getFkdrColor(double fkdr) {
        if (fkdr >= 20) return "§5"; // Dark Purple
        if (fkdr >= 15) return "§d"; // Light Purple
        if (fkdr >= 10) return "§4"; // Dark Red
        if (fkdr >= 8)  return "§c"; // Red
        if (fkdr >= 6)  return "§6"; // Gold
        if (fkdr >= 4)  return "§e"; // Yellow
        if (fkdr >= 2)  return "§2"; // Dark Green
        if (fkdr >= 1)  return "§a"; // Green
        if (fkdr >= 0.5) return "§f"; // White
        return "§7"; // Light Gray（0未満〜0.49）
    }

    private static String getWlrColor(double wlr) {
        if (wlr >= 10) return "§5"; // Dark Purple
        if (wlr >= 8)  return "§d"; // Light Purple
        if (wlr >= 6)  return "§4"; // Dark Red
        if (wlr >= 5)  return "§c"; // Red
        if (wlr >= 4)  return "§6"; // Gold
        if (wlr >= 3)  return "§e"; // Yellow
        if (wlr >= 2)  return "§2"; // Dark Green
        if (wlr >= 1)  return "§a"; // Green
        if (wlr >= 0.5) return "§f"; // White
        return "§7"; // Light Gray（0未満〜0.49）
    }


    private static String getWinsColor(int wins) {
        if (wins >= 50000) return "§5"; // Dark Purple
        if (wins >= 25000) return "§d"; // Light Purple
        if (wins >= 10000) return "§4"; // Dark Red
        if (wins >= 5000) return "§c"; // Red
        if (wins >= 2500) return "§6"; // Gold
        if (wins >= 1000) return "§e"; // Yellow
        if (wins >= 500) return "§2";  // Dark Green
        if (wins >= 250) return "§a";  // Green
        if (wins >= 50) return "§f";  // White
        return "§7"; // Gray
    }

    private static String getFinalsColor(int finals) {
        if (finals >= 100000) return "§5"; // Dark Purple
        if (finals >= 50000) return "§d"; // Light Purple
        if (finals >= 25000) return "§4"; // Dark Red
        if (finals >= 10000) return "§c"; // Red
        if (finals >= 5000) return "§6"; // Gold
        if (finals >= 2500) return "§e"; // Yellow
        if (finals >= 1000) return "§2";  // Dark Green
        if (finals >= 500) return "§a";  // Green
        if (finals >= 100) return "§f";  // White
        return "§7"; // Gray
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
        if (displayRank == null) return "§7"; // No rank
        String plusColor = "§c";
        if (rankPlusColorStr != null) {
            EnumChatFormatting color = EnumChatFormatting.getValueByName(rankPlusColorStr);
            if(color != null) {
                plusColor = color.toString();
            }
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

    private static void sendInvalidApiKeyMessage() {
        sendMessageToPlayer(EnumChatFormatting.RED + "Your Hypixel API key is invalid!");
        sendMessageToPlayer(EnumChatFormatting.YELLOW + "Please get a new one with /api new");
    }

    private static void sendMessageToPlayer(String message) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
            }
        });
    }
}