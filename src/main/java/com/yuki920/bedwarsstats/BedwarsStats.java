package com.yuki920.bedwarsstats;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = BedwarsStats.MODID, version = BedwarsStats.VERSION, name = BedwarsStats.NAME, clientSideOnly = true, guiFactory = "com.yuki920.bedwarsstats.GuiFactory")
public class BedwarsStats {
    public static final String MODID = "bedwarsstats";
    public static final String VERSION = "0.1.0";
    public static final String NAME = "Bedwars Stats";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.init(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // イベントハンドラとコマンドを登録
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerJoinEventHandler());
        ClientCommandHandler.instance.registerCommand(new BwmCommand());
    }
}
