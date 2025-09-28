package com.yuki920.bedwarsstats;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

public class PlayerJoinEventHandler {
    private boolean firstJoin = true;

    @SubscribeEvent
    public void onPlayerJoin(ClientConnectedToServerEvent event) {
        // Only run this on the first time the player joins a server per session
        if (firstJoin) {
            HypixelApiHandler.checkApiKeyValidity();
            firstJoin = false;
        }
    }
}