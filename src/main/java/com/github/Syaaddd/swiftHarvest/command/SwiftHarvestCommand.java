package com.github.Syaaddd.swiftHarvest.command;

import com.github.Syaaddd.swiftHarvest.SwiftHarvest;
import com.github.Syaaddd.swiftHarvest.util.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SwiftHarvestCommand implements CommandExecutor {

    private final SwiftHarvest plugin;

    public SwiftHarvestCommand(SwiftHarvest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle /sh toggle (available to all players with use permission)
        if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtils.colorize("&cThis command can only be used by players."));
                return true;
            }

            if (!sender.hasPermission("swifthavert.use")) {
                sender.sendMessage(MessageUtils.colorize("&cYou do not have permission to use this command."));
                return true;
            }

            // Check if toggle mode is enabled
            if (!plugin.getConfigManager().getActivationMode().equalsIgnoreCase("toggle")) {
                sender.sendMessage(MessageUtils.colorize("&cToggle mode is not enabled. Current mode: " + 
                    plugin.getConfigManager().getActivationMode()));
                return true;
            }

            Player player = (Player) sender;
            plugin.getActivationManager().toggle(player);
            boolean enabled = plugin.getActivationManager().isToggledOn(player);

            if (enabled) {
                player.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getMessage("toggle-on")));
            } else {
                player.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getMessage("toggle-off")));
            }
            return true;
        }

        // Admin commands below
        if (!sender.hasPermission("swifthavert.admin")) {
            sender.sendMessage(MessageUtils.colorize("&cYou do not have permission to use this command."));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reload();
            plugin.getCooldownManager().clearAll();
            sender.sendMessage(MessageUtils.colorize(plugin.getConfigManager().getMessage("config-reloaded")));
            return true;
        }

        sender.sendMessage(MessageUtils.colorize("&7=== SwiftHarvest v1.3.0 ==="));
        sender.sendMessage(MessageUtils.colorize("&b/swifthavert reload &7- Reload configuration"));
        sender.sendMessage(MessageUtils.colorize("&b/swifthavert toggle &7- Toggle VeinMiner/Timber (players)"));
        return true;
    }
}
