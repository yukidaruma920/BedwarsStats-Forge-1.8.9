package com.yuki920.bedwarsstats;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class BwmCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "bwm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bwm setkey <api_key>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        // 誰でも実行できるように0を返す
        return 0;
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
        } 
        // ★★★ statsサブコマンドを追加 ★★★
        else if (args.length == 2 && args[0].equalsIgnoreCase("stats")) {
            HypixelApiHandler.processPlayer(args[1]);
        }
        else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
        }
    }
    
    // ★★★ getCommandUsageも更新 ★★★
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bwm <setapikey <key>|stats <username>>";
    }
}
