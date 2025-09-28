package com.yuki920.bedwarsstats;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ModConfigGui extends GuiConfig {

    public ModConfigGui(GuiScreen parentScreen) {
        super(parentScreen,
            new ConfigElement(ConfigHandler.config.getCategory(ConfigHandler.CATEGORY_GENERAL)).getChildElements(),
            BedwarsStats.MODID,
            false,
            false,
            GuiConfig.getAbridgedConfigPath(ConfigHandler.config.toString()));
    }
}