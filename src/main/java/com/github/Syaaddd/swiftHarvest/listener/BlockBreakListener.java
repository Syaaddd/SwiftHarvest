package com.github.Syaaddd.swiftHarvest.listener;

import com.github.Syaaddd.swiftHarvest.SwiftHarvest;
import com.github.Syaaddd.swiftHarvest.config.SwiftHarvestConfig;
import com.github.Syaaddd.swiftHarvest.integration.WorldGuardHook;
import com.github.Syaaddd.swiftHarvest.manager.CooldownManager;
import com.github.Syaaddd.swiftHarvest.manager.DurabilityManager;
import com.github.Syaaddd.swiftHarvest.manager.DropManager;
import com.github.Syaaddd.swiftHarvest.scanner.BlockScanner;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BlockBreakListener implements Listener {

    private final SwiftHarvest plugin;
    private final SwiftHarvestConfig config;
    private final CooldownManager cooldownManager;

    public BlockBreakListener(SwiftHarvest plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.cooldownManager = plugin.getCooldownManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (config.isWorldBlacklisted(block.getWorld().getName())) {
            return;
        }

        if (WorldGuardHook.isEnabled() && !WorldGuardHook.canBuild(player, block)) {
            return;
        }

        boolean requireSneak = config.isRequireSneak();
        if (requireSneak && !player.isSneaking()) {
            return;
        }

        if (config.isVeinMinerEnabled()) {
            if (handleVeinMiner(event, player, block, blockType, tool)) {
                return;
            }
        }

        if (config.isTimberEnabled()) {
            handleTimber(event, player, block, blockType, tool);
        }
    }

    private boolean handleVeinMiner(BlockBreakEvent event, Player player, Block block, Material blockType, ItemStack tool) {
        if (!config.getVeinMinerBlocks().contains(blockType)) {
            return false;
        }

        if (!config.getVeinMinerTools().contains(tool.getType())) {
            return false;
        }

        if (cooldownManager.isOnCooldown(player)) {
            long remaining = cooldownManager.getRemainingCooldown(player);
            String message = config.getMessage("cooldown").replace("%seconds%", String.valueOf(remaining));
            player.sendMessage(message);
            return false;
        }

        int maxBlocks = config.getMaxBlocks();
        List<Block> connectedBlocks = BlockScanner.findConnectedBlocks(block, blockType, maxBlocks);

        if (connectedBlocks.size() <= 1) {
            return false;
        }

        if (!DurabilityManager.hasEnoughDurability(tool, connectedBlocks.size())) {
            player.sendMessage(config.getMessage("low-durability"));
            return false;
        }

        event.setCancelled(true);

        List<ItemStack> allDrops = new ArrayList<>();
        for (Block b : connectedBlocks) {
            for (ItemStack drop : b.getDrops(tool)) {
                allDrops.add(drop);
            }
            b.setType(Material.AIR);
        }

        List<ItemStack> mergedDrops = DropManager.mergeDrops(allDrops);
        boolean inventoryFull = !DropManager.consolidateDrops(player, mergedDrops);

        if (inventoryFull) {
            player.sendMessage(config.getMessage("full-inventory"));
        }

        DurabilityManager.reduceDurability(tool, connectedBlocks.size());
        
        cooldownManager.setCooldown(player);

        player.sendMessage(config.getMessage("veinminer-started").replace("%amount%", String.valueOf(connectedBlocks.size())));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        return true;
    }

    private boolean handleTimber(BlockBreakEvent event, Player player, Block block, Material blockType, ItemStack tool) {
        if (!config.getTimberBlocks().contains(blockType)) {
            return false;
        }

        if (!config.getTimberTools().contains(tool.getType())) {
            return false;
        }

        if (cooldownManager.isOnCooldown(player)) {
            long remaining = cooldownManager.getRemainingCooldown(player);
            String message = config.getMessage("cooldown").replace("%seconds%", String.valueOf(remaining));
            player.sendMessage(message);
            return false;
        }

        int maxBlocks = config.getMaxBlocks();
        boolean breakLeaves = config.isBreakLeaves();

        List<Block> connectedBlocks = BlockScanner.findConnectedTimber(
            block, 
            config.getTimberBlocks(), 
            config.getLeafBlocks(), 
            maxBlocks, 
            breakLeaves
        );

        if (connectedBlocks.size() <= 1) {
            return false;
        }

        if (!DurabilityManager.hasEnoughDurability(tool, connectedBlocks.size())) {
            player.sendMessage(config.getMessage("low-durability"));
            return false;
        }

        event.setCancelled(true);

        List<ItemStack> allDrops = new ArrayList<>();
        for (Block b : connectedBlocks) {
            for (ItemStack drop : b.getDrops(tool)) {
                allDrops.add(drop);
            }
            b.setType(Material.AIR);
        }

        List<ItemStack> mergedDrops = DropManager.mergeDrops(allDrops);
        boolean inventoryFull = !DropManager.consolidateDrops(player, mergedDrops);

        if (inventoryFull) {
            player.sendMessage(config.getMessage("full-inventory"));
        }

        DurabilityManager.reduceDurability(tool, connectedBlocks.size());
        
        cooldownManager.setCooldown(player);

        player.sendMessage(config.getMessage("timber-started").replace("%amount%", String.valueOf(connectedBlocks.size())));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        return true;
    }
}
