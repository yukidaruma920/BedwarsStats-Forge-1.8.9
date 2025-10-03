package com.yuki920.bedwarsstats;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

public class PlayerJoinEventHandler {
    private static boolean hasChecked = false;

    @SubscribeEvent
    public void onPlayerJoin(ClientConnectedToServerEvent event) {
        // Only run this check once per game launch to avoid spamming the API
        if (!hasChecked) {
            HypixelApiHandler.checkApiKeyValidity();
            hasChecked = true;
        }
    }
}