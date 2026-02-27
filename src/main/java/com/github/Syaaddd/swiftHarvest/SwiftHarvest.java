package com.github.Syaaddd.swiftHarvest;

import com.github.Syaaddd.swiftHarvest.command.SwiftHarvestCommand;
import com.github.Syaaddd.swiftHarvest.config.SwiftHarvestConfig;
import com.github.Syaaddd.swiftHarvest.integration.WorldGuardHook;
import com.github.Syaaddd.swiftHarvest.listener.BlockBreakListener;
import com.github.Syaaddd.swiftHarvest.listener.PlayerInteractListener;
import com.github.Syaaddd.swiftHarvest.manager.CooldownManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SwiftHarvest extends JavaPlugin {

    private SwiftHarvestConfig configManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        getLogger().info("SwiftHarvest v1.0.0-SNAPSHOT loading...");

        configManager = new SwiftHarvestConfig(this);
        cooldownManager = new CooldownManager();
        cooldownManager.setCooldownTicks(configManager.getCooldown());

        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);

        getCommand("swifthavert").setExecutor(new SwiftHarvestCommand(this));

        Plugin worldGuard = getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null) {
            WorldGuardHook.init(worldGuard);
            getLogger().info("WorldGuard integration enabled!");
        }

        getLogger().info("SwiftHarvest enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SwiftHarvest disabled.");
    }

    public SwiftHarvestConfig getConfigManager() {
        return configManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}
