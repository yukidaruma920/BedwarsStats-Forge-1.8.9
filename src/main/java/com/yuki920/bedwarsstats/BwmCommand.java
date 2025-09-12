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
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid usage. " + getCommandUsage(sender)));
            return;
        }

        if (args[0].equalsIgnoreCase("setkey")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Please provide an API key. Usage: /bwm setkey <api_key>"));
                return;
            }
            String apiKey = args[1];
            ConfigHandler.setApiKey(apiKey);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Hypixel API Key has been set."));
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown subcommand. " + getCommandUsage(sender)));
        }
    }
}
