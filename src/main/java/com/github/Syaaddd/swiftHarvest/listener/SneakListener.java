package com.github.Syaaddd.swiftHarvest.listener;

import com.github.Syaaddd.swiftHarvest.SwiftHarvest;
import com.github.Syaaddd.swiftHarvest.config.SwiftHarvestConfig;
import com.github.Syaaddd.swiftHarvest.manager.ActivationManager;
import com.github.Syaaddd.swiftHarvest.manager.ParticleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Handles sneak events for "hold" activation mode.
 * Tracks how long player holds sneak and activates when threshold reached.
 */
public class SneakListener implements Listener {

    private final SwiftHarvest plugin;
    private final SwiftHarvestConfig config;
    private final ActivationManager activationManager;

    public SneakListener(SwiftHarvest plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.activationManager = plugin.getActivationManager();
    }

    @EventHandler
    public void onSneakToggle(PlayerToggleSneakEvent event) {
        // Only handle hold mode
        if (!config.getActivationMode().equalsIgnoreCase("hold")) {
            return;
        }

        Player player = event.getPlayer();
        int requiredTicks = config.getHoldTicks();

        if (player.isSneaking()) {
            // Started sneaking - start counting
            // Reset any previous hold state
            activationManager.releaseHold(player);
        } else {
            // Stopped sneaking - reset hold
            activationManager.releaseHold(player);
            ParticleManager.clearPreview(player);
        }
    }

    /**
     * Called every tick by the main plugin to update hold state.
     */
    public static void tickHoldCheck(SwiftHarvest plugin, Player player) {
        if (!plugin.getConfigManager().getActivationMode().equalsIgnoreCase("hold")) {
            return;
        }

        if (!player.isSneaking()) {
            plugin.getActivationManager().releaseHold(player);
            return;
        }

        int requiredTicks = plugin.getConfigManager().getHoldTicks();
        plugin.getActivationManager().tickHold(player, requiredTicks);
    }
}
