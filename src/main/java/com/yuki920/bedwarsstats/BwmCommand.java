package com.yuki920.bedwarsstats;

import com.yuki920.bedwarsstats.libs.moulconfig.MoulConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BwmCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "bwm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bwm <stats <player> [mode] | settings <apikey|mode|nick> <value>>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            openConfigGui();
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "stats":
                processStatsCommand(sender, args);
                break;
            case "settings":
                processSettingsCommand(sender, args);
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    private void openConfigGui() {
        Minecraft.getMinecraft().addScheduledTask(() ->
            Minecraft.getMinecraft().displayGuiScreen(MoulConfig.getGui(BedwarsStats.config))
        );
    }

    private void processStatsCommand(ICommandSender sender, String[] args) throws WrongUsageException {
        if (args.length < 2) {
            throw new WrongUsageException("/bwm stats <player> [mode]");
        }
        String playerName = args[1];
        DisplayMode mode = BedwarsStats.config.displayMode; // Default mode

        if (args.length > 2) {
            try {
                mode = DisplayMode.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid mode! Valid modes are: " +
                    Arrays.stream(DisplayMode.values()).map(Enum::name).collect(Collectors.joining(", "))));
                return;
            }
        }

        HypixelApiHandler.processPlayer(playerName, mode);
    }

    private void processSettingsCommand(ICommandSender sender, String[] args) throws WrongUsageException {
        if (args.length < 3) {
            throw new WrongUsageException("/bwm settings <apikey|mode|nick> <value>");
        }
        String setting = args[1].toLowerCase();
        String value = args[2];

        switch (setting) {
            case "apikey":
                BedwarsStats.config.apiKey = value;
                BedwarsStats.config.save();
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "API Key set successfully!"));
                break;
            case "mode":
                try {
                    BedwarsStats.config.displayMode = DisplayMode.valueOf(value.toUpperCase());
                    BedwarsStats.config.save();
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Display mode set to " + value));
                } catch (IllegalArgumentException e) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid mode! Valid modes are: " +
                        Arrays.stream(DisplayMode.values()).map(Enum::name).collect(Collectors.joining(", "))));
                }
                break;
            case "nick":
                BedwarsStats.config.nick = value;
                BedwarsStats.config.save();
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Nickname set to " + value));
                break;
            default:
                throw new WrongUsageException("/bwm settings <apikey|mode|nick> <value>");
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "stats", "settings");
        }

        String subCommand = args[0].toLowerCase();
        if ("stats".equals(subCommand)) {
            if (args.length == 2) {
                // Player name completion
                List<String> playerNames = Minecraft.getMinecraft().theWorld.playerEntities.stream()
                    .map(player -> player.getName())
                    .collect(Collectors.toList());
                return getListOfStringsMatchingLastWord(args, playerNames);
            } else if (args.length == 3) {
                // Mode completion
                List<String> modes = Arrays.stream(DisplayMode.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList());
                return getListOfStringsMatchingLastWord(args, modes);
            }
        } else if ("settings".equals(subCommand)) {
            if (args.length == 2) {
                return getListOfStringsMatchingLastWord(args, "apikey", "mode", "nick");
            } else if (args.length == 3 && "mode".equalsIgnoreCase(args[1])) {
                // Mode completion for settings
                List<String> modes = Arrays.stream(DisplayMode.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList());
                return getListOfStringsMatchingLastWord(args, modes);
            }
        }

        return null;
    }
}