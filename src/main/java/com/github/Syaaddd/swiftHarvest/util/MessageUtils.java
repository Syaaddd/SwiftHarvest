package com.github.Syaaddd.swiftHarvest.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public class MessageUtils {

    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fk-or])");

    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    public static void sendRawMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }
}
