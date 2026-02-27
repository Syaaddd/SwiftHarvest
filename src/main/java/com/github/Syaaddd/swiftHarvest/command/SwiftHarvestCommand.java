package com.github.Syaaddd.swiftHarvest.command;

import com.github.Syaaddd.swiftHarvest.SwiftHarvest;
import com.github.Syaaddd.swiftHarvest.util.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SwiftHarvestCommand implements CommandExecutor {

    private final SwiftHarvest plugin;

    public SwiftHarvestCommand(SwiftHarvest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("swifthavert.admin")) {
            sender.sendMessage(MessageUtils.colorize("&cKamu tidak memiliki izin untuk menggunakan perintah ini."));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reload();
            plugin.getCooldownManager().clearAll();
            sender.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getMessage("config-reloaded")));
            return true;
        }

        sender.sendMessage(MessageUtils.colorize("&7=== SwiftHarvest ==="));
        sender.sendMessage(MessageUtils.colorize("&b/swifthavert reload &7- Muat ulang konfigurasi"));
        return true;
    }
}
