package com.yuki920.bedwarsstats;

import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatEventHandler {
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        // type 0 は通常のチャットメッセージ
        if (event.type != 0) {
            return;
        }

        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (message.startsWith("ONLINE: ")) {
            String playersPart = message.substring("ONLINE: ".length());
            String[] playerNames = playersPart.split(", ");

            for (String name : playerNames) {
                // 各プレイヤーの統計情報を非同期で取得
                HypixelApiHandler.processPlayer(name.trim());
            }
        }
    }
}
