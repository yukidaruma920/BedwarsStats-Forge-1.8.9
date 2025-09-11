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

    public static void processPlayer(String username) {
        // ゲームをフリーズさせないように、別スレッドで処理を実行
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Mojang APIからUUIDを取得
                String mojangUrl = "https://api.mojang.com/users/profiles/minecraft/" + username;
                String mojangResponse = sendHttpRequest(mojangUrl, null);
                if (mojangResponse == null) return;

                JsonObject mojangJson = GSON.fromJson(mojangResponse, JsonObject.class);
                if (!mojangJson.has("id")) return;
                String uuid = mojangJson.get("id").getAsString();

                // 2. Hypixel APIから統計情報を取得
                if (ConfigHandler.apiKey == null || ConfigHandler.apiKey.isEmpty()) {
                    sendMessageToPlayer(EnumChatFormatting.RED + "Hypixel API Key not set!");
                    return;
                }
                String hypixelUrl = HYPIXEL_API_URL + uuid;
                String hypixelResponse = sendHttpRequest(hypixelUrl, ConfigHandler.apiKey);
                if (hypixelResponse == null) return;
                
                JsonObject hypixelJson = GSON.fromJson(hypixelResponse, JsonObject.class);
                if (!hypixelJson.has("player") || hypixelJson.get("player").isJsonNull()) {
                    return;
                }
                
                JsonObject player = hypixelJson.getAsJsonObject("player");

                // 3. 統計情報をフォーマットして表示
                String formattedMessage = formatStats(player);
                if (formattedMessage != null) {
                    sendMessageToPlayer(formattedMessage);
                }

            } catch (Exception e) {
                // エラーはコンソールに出力
                e.printStackTrace();
            }
        });
    }

    private static String formatStats(JsonObject player) {
        String username = player.get("displayname").getAsString();
        String rankColor = getRankColor(player);
        
        if (!player.has("stats") || player.get("stats").isJsonNull() || !player.getAsJsonObject("stats").has("Bedwars")) {
             return rankColor + username + EnumChatFormatting.GRAY + ": No Bedwars stats found.";
        }
        
        JsonObject bedwars = player.getAsJsonObject("stats").getAsJsonObject("Bedwars");

        int stars = (player.has("achievements") && player.getAsJsonObject("achievements").has("bedwars_level"))
                ? player.getAsJsonObject("achievements").get("bedwars_level").getAsInt() : 0;
        
        int wins = bedwars.has("wins_bedwars") ? bedwars.get("wins_bedwars").getAsInt() : 0;
        int losses = bedwars.has("losses_bedwars") ? bedwars.get("losses_bedwars").getAsInt() : 0;
        int finalKills = bedwars.has("final_kills_bedwars") ? bedwars.get("final_kills_bedwars").getAsInt() : 0;
        int finalDeaths = bedwars.has("final_deaths_bedwars") ? bedwars.get("final_deaths_bedwars").getAsInt() : 0;

        double wlr = (losses == 0) ? wins : (double) wins / losses;
        double fkdr = (finalDeaths == 0) ? finalKills : (double) finalKills / finalDeaths;

        String prestige = PrestigeFormatter.formatPrestige(stars);

        return String.format("%s %s%s§r: §aWins §f%d §7| §aWLR §f%.2f §7| §aFinals §f%d §7| §aFKDR §f%.2f",
                prestige, rankColor, username, wins, wlr, finalKills, fkdr);
    }
    
    private static String getRankColor(JsonObject player) {
        String rank = "";
        if (player.has("rank")) {
            rank = player.get("rank").getAsString();
        } else if (player.has("newPackageRank")) {
            rank = player.get("newPackageRank").getAsString();
        } 

        switch (rank) {
            case "VIP":
            case "VIP_PLUS":
                return "§a"; // Green
            case "MVP":
            case "MVP_PLUS":
                return "§b"; // Aqua
            case "SUPERSTAR": // MVP++
                return "§6"; // Gold
            case "YOUTUBER":
            case "ADMIN":
            case "OWNER":
                return "§c"; // Red
            default:
                return "§7"; // Gray
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
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            return content.toString();
        }
        return null;
    }

    private static void sendMessageToPlayer(String message) {
        // Minecraftのメインスレッドでチャットメッセージを送信
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
            }
        });
    }
}
