package com.yuki920.bedwarsstats;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class BwmCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "bwm";
    }
    
    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("bedwarsmodule");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bwm setapikey <key>";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0; // 誰でも実行可能
    }
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 2 && args[0].equalsIgnoreCase("setapikey")) {
            ConfigHandler.setApiKey(args[1]);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "API Key set successfully!"));
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
        }
    }
}
