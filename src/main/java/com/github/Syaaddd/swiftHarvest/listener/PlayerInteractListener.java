package com.github.Syaaddd.swiftHarvest.listener;

import com.github.Syaaddd.swiftHarvest.SwiftHarvest;
import com.github.Syaaddd.swiftHarvest.config.SwiftHarvestConfig;
import com.github.Syaaddd.swiftHarvest.manager.ActivationManager;
import com.github.Syaaddd.swiftHarvest.manager.ParticleManager;
import com.github.Syaaddd.swiftHarvest.scanner.BlockScanner;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerInteractListener implements Listener {

    private final SwiftHarvest plugin;
    private final SwiftHarvestConfig config;

    public PlayerInteractListener(SwiftHarvest plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        if (!config.isPreviewParticles()) return;

        Player player = event.getPlayer();
        Block targetBlock = player.getTargetBlock(null, 5);

        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            return;
        }

        if (config.isWorldBlacklisted(targetBlock.getWorld().getName())) {
            return;
        }

        Material blockType = targetBlock.getType();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Check activation conditions for preview
        if (!shouldShowPreview(player)) {
            ParticleManager.clearPreview(player);
            return;
        }

        if (config.isVeinMinerEnabled()) {
            if (config.getVeinMinerBlocks().contains(blockType) && 
                config.getVeinMinerTools().contains(tool.getType())) {
                
                boolean diagonal = config.getVeinMinerScanMode().equalsIgnoreCase("diagonal");
                int maxBlocks = config.getMaxBlocksForVeinMiner(tool.getType());
                
                List<Block> preview = BlockScanner.findVeinPreview(
                    targetBlock, 
                    blockType, 
                    maxBlocks,
                    diagonal
                );
                if (preview.size() > 1) {
                    ParticleManager.showVeinPreview(player, preview);
                    return;
                }
            }
        }

        if (config.isTimberEnabled()) {
            if (config.getTimberBlocks().contains(blockType) && 
                config.getTimberTools().contains(tool.getType())) {
                
                boolean strict = config.getTimberDetection().equalsIgnoreCase("strict");
                int maxRadius = config.getMaxHorizontalRadius();
                int maxBlocks = config.getMaxBlocksForTimber(tool.getType());
                
                List<Block> preview = BlockScanner.findTimberPreview(
                    targetBlock,
                    config.getTimberBlocks(),
                    config.getLeafBlocks(),
                    maxBlocks,
                    config.isBreakLeaves(),
                    strict,
                    maxRadius
                );
                if (preview.size() > 1) {
                    ParticleManager.showTimberPreview(player, preview);
                }
            }
        }
    }

    private boolean shouldShowPreview(Player player) {
        String mode = config.getActivationMode();
        ActivationManager activationManager = plugin.getActivationManager();
        
        switch (mode.toLowerCase()) {
            case "toggle":
                return activationManager.isToggledOn(player);
            case "hold":
                return activationManager.isHoldActive(player);
            case "sneak":
            default:
                return config.isRequireSneak() ? player.isSneaking() : true;
        }
    }
}
